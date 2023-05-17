/*
 *----------------------------------------------------------------------------*
 * COPYRIGHT NOTICE                                                           *
 * Copyright 2014 The Sherwin-Williams Company All Rights Reserved.           *
 *                                                                            *
 * This program code may be used and modified by employees of The             *
 * Sherwin-Williams Company so long as this copyright notice and the comments *
 * above remain intact.                                                       *
 *                                                                            *
 * Selling the code from this program without prior written consent is        *
 * expressly forbidden.  User agrees that Sherwin-Williams is the final       *
 * arbiter of this application and subject matter of this Word Product.       *
 * All inventions and work product conceived, made, or created by the Users   *
 * either solely or jointly with Sherwin-Williams, in the course of User's    *
 * performance of the Services shall become and remain the exclusive property *
 * of Sherwin-Williams.                                                       *
 *                                                                            *
 * Obtain permission before redistributing this software over the Internet or *
 * in any other medium.  In all cases copyright and header must remain intact *
 *----------------------------------------------------------------------------*
 */
package com.sherwin.ireredux;

import static com.sherwin.ireredux.ImageRenderingConstants.SURFACE_DATA_DELIMITER;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import com.sherwin.sd.conversion.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for masking the correct surface data to
 * the base image.
 */

@Slf4j
public class SurfaceMasking
{
    /**
     * Based on the surfaceMaskMap (a collection of surface names mapped to
     * lists of surface masks), apply the masking to the image.
     *
     * @param baseImage
     * @param surfaceMaskMap
     * @param parameterMap
     * @throws Exception
     */
    protected static void maskSurfaceData( BufferedImage baseImage,
                                           Map<String, String> surfaceMaskMap, Map<String, String> parameterMap )
            throws Exception
    {
        final int baseWidth = baseImage.getWidth();
        final int baseHeight = baseImage.getHeight();
        maskSurfaceData( baseImage, surfaceMaskMap, parameterMap, baseWidth, baseHeight );
    }

    /**
     * Based on the surfaceMaskMap (a collection of surface names mapped to
     * lists of surface masks), apply the masking to the image
     *
     * @param baseImage
     * @param surfaceMaskMap
     * @param parameterMap
     * @param originalImageWidth
     * @param originalImageHeight
     * @throws Exception
     */

    protected static void maskSurfaceData( BufferedImage baseImage,
                                           Map<String, String> surfaceMaskMap, Map<String, String> parameterMap,
                                           int originalImageWidth, int originalImageHeight )
            throws Exception
    {
        long startTime = System.currentTimeMillis();

        /*
         * Make a copy of the baseImage as we will need to keep reading
         * back the original pixels for each mask while the baseImage actually
         * gets modified.
         */
        BufferedImage origImage = new BufferedImage( baseImage.getWidth(), baseImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB );
        origImage.getGraphics().drawImage( baseImage, 0, 0, null );

        /*
         * Build the collection of unique surface mask objects. If there
         * are duplicate masks in the xml file then these entities will
         * provide a way to perform the expensive masking process only
         * once per mask.
         */
        List<UniqueSurfaceMask> uniqueSurfaceMasks = collectUniqueSurfaceMasks( surfaceMaskMap );
        String startSurfaceName = getSurfaceNamingConvention( uniqueSurfaceMasks );

        /*
         * Create a map of surface names and associated colors from the
         * incoming request parameters.
         */
        Map<String, String> surfaceColorMap = new HashMap<String, String>();

        // iterate over request parameters, pulling the surface names and colors
        for( Map.Entry<String, String> entry : parameterMap.entrySet() )
        {
            // get the map key:values
            String paramKey = entry.getKey();
            String paramValue = entry.getValue();

            //look for parameters that are surface names
            if( paramKey.startsWith( startSurfaceName ) )
            {
                int firstIndex = paramValue.indexOf( SURFACE_DATA_DELIMITER );
                paramValue = paramValue.substring( firstIndex + 1 );

                // get the color/paint value from the surface data
                if( paramValue.startsWith( "paint" ) )
                {
                    int secondIndex = paramValue.indexOf( SURFACE_DATA_DELIMITER );
                    paramValue = paramValue.substring( secondIndex + 1 );

                    int lastIndex = paramValue.indexOf( SURFACE_DATA_DELIMITER );
                    paramValue = paramValue.substring( 0, lastIndex );
                }

                // add the surface name(key) and color(value) to the map
                surfaceColorMap.put( paramKey, paramValue );
            }
        }

        // iterate over surfaces from the request
        for( Map.Entry<String, String> entry : surfaceColorMap.entrySet() )
        {
            // get the map key:values
            String surfaceName = entry.getKey();
            String surfaceColor = entry.getValue();

            // iterate over the unique surface masks
            for( UniqueSurfaceMask usm : uniqueSurfaceMasks )
            {
                // check if this surface name is in the unique surface mask
                if( usm.contains( surfaceName ) )
                {
                    // check if the surface mask has be used/processed yet
                    if( !usm.isUsed() )
                    {
                        usm.setIsUsed( true );

                        byte[] maskBytes = null;
                        try
                        {
                            // base64 decode
                            maskBytes = Base64.decode( usm.getMask() );

                            // zlib decompress
                            maskBytes = zlibDecompress( maskBytes );
                        }
                        catch( Exception e )
                        {
                            /*
                             * Any exception encountered with decoding or decompressing
                             * is most likely due to some sort of data corruption. Ignore
                             * this masking region.
                             */
                            log.error( "Error encountered with masking data: " + e );
                            break;
                        }

                        /*
                         *  If there is any data in any channel, it should be considered
                         *  part of the mask.
                         */

                        /*
                         * there are scenes where the surface mask is the not same size as the original image
                         * in these cases, create the mask byte array based upon the original image width & height,
                         * not the size of the decompressed surface mask from the XML file.
                         */
                        byte[] newBytes;
                        if (originalImageWidth * originalImageHeight * 4 > maskBytes.length)
                            newBytes = new byte[ (originalImageWidth * originalImageHeight * 4) + 1 ];
                        else
                            newBytes = new byte[ maskBytes.length + 1 ];

                        int i = 0;
                        for( byte b : maskBytes )
                        {
                            i++ ;
                            if( b != 0 )
                            {
                                b = (byte) 255;
                                newBytes[ i ] = b;
                            }
                        }

                        // load byte[] pixel data into image object
                        BufferedImage maskImage = new BufferedImage( originalImageWidth,
                                originalImageHeight, BufferedImage.TYPE_4BYTE_ABGR );
                        maskImage.getRaster().setDataElements( 0, 0, originalImageWidth,
                                originalImageHeight, newBytes );

                        /*
                         * Only resize the maskImage if the dimensions of the base image do not match
                         * the dimensions of the original image
                         */
                        if( baseImage.getWidth() != originalImageWidth
                                || baseImage.getHeight() != originalImageHeight )
                        {
                            maskImage = ImageUtil.resizeImage( maskImage, baseImage.getWidth(),
                                    baseImage.getHeight() );
                        }

                        // create a Color object from the color number in the request
                        Color aColor = new Color( Integer.parseInt( surfaceColor ) );

                        // apply the masking to the base image using the appropriate color algorithm
                        ImageColorization.applyColor( origImage, baseImage, maskImage, aColor );

                        // break out of the loop of UniqueSurfaceMasks and continue looping
                        // the surfaceColorMap entries
                        break;
                    }
                }
            }
        }

//        if( log.isDebugEnabled() )
//        {
//            log.debug( "Time to mask all regions: " + (System.currentTimeMillis() - startTime)
//                    + " ms" );
//        }
    }

    /*
     * Use zlib decompression to inflate the surface data
     */
    private static byte[] zlibDecompress( byte[] maskBytes )
            throws Exception
    {
        Inflater decompresser = new Inflater();
        decompresser.setInput( maskBytes );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( maskBytes.length );

        // finished() never returns true for some byte arrays, thus also check the inflater count
        int count = -1;
        byte[] buffer = new byte[ 2048 ];
        while( !decompresser.finished() && count != 0)
        {
            count = decompresser.inflate( buffer );
            outputStream.write( buffer, 0, count );
        }
        decompresser.end();
        outputStream.close();
        return outputStream.toByteArray();
    }

    /*
     * Interrogate the surface map for duplicates and break them out into
     * UniqueSurfaceMask objects
     */
    protected static List<UniqueSurfaceMask> collectUniqueSurfaceMasks(
            Map<String, String> surfaceMaskMap )
    {
        long dupeTime = System.currentTimeMillis();
        List<UniqueSurfaceMask> uniqueSurfaceMasks = new ArrayList<UniqueSurfaceMask>();
        boolean hasDupes = false;

        // iterate over map of surface masks
        for( Map.Entry<String, String> entry : surfaceMaskMap.entrySet() )
        {
            // get the map key:values
            String surfaceName = entry.getKey();
            String mask = entry.getValue();

            // check if the mask is the same as any of UniqueSurfaceMasks
            boolean usmFound = false;
            for( UniqueSurfaceMask usm : uniqueSurfaceMasks )
            {
                if( usm.getMask().equals( mask ) )
                {
                    hasDupes = true;
                    usmFound = true;
                    usm.addSurfaceName( surfaceName );
                }
            }

            /*
             * If a UniqueSurfaceMask hasn't been created for this
             * mask yet, create one and add it to the list
             */
            if( !usmFound )
            {
                uniqueSurfaceMasks.add( new UniqueSurfaceMask( mask, surfaceName ) );
            }
        }

        if( log.isDebugEnabled() )
        {
            log.debug( "*** duplicate surfaces exist: " + hasDupes + " - nbr distinct masks: "
                    + uniqueSurfaceMasks.size() + " - check took "
                    + (System.currentTimeMillis() - dupeTime) + " ms" );
        }

        return uniqueSurfaceMasks;
    }

    /*
     * Return the beginning of the surface names as they are defined
     * in the more structured xml data than in the willy-nilly request
     * data.
     */
    protected static String getSurfaceNamingConvention( List<UniqueSurfaceMask> uniqueSurfaceMasks )
    {
        for( UniqueSurfaceMask usm : uniqueSurfaceMasks )
        {
            for( String surfaceName : usm.getSurfaceNames() )
            {
                return surfaceName.substring( 0, 7 );
            }
        }
        // default to Surface
        return "Surface";
    }

}

