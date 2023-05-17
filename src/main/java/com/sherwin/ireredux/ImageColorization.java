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

import lombok.extern.slf4j.Slf4j;

import static com.sherwin.ireredux.ImageRenderingConstants.MITIGATED_MAX;
import static com.sherwin.ireredux.ImageRenderingConstants.MITIGATED_MIN;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class handles all the image colorization magic
 */
@Slf4j
public class ImageColorization
{
    private static int red = 0;
    private static int green = 1;
    private static int blue = 2;
    private static int alpha = 3;

    /**
     * Apply the surface color using the appropriate color algorithm to the base
     * image where the masking image overlays it
     *
     * @param baseImage
     * @param maskImage
     * @param surfaceColor the color to apply to masked section of the image
     * @throws Exception
     */
    protected static void applyColor( BufferedImage origImage, BufferedImage baseImage,
                                      BufferedImage maskImage, Color surfaceColor )
            throws Exception
    {
        long startTime = System.currentTimeMillis();

        paintProcess( origImage, baseImage, maskImage, surfaceColor );

        long endTime = System.currentTimeMillis();

        if( log.isDebugEnabled() )
        {
            log.debug( "Time to execute: " + (endTime - startTime) + " ms" );
        }
    }

    /**
     * Paint the pixels onto the mask from the base image. Desaturate the masked
     * portion of the base image. Mitigate the image to reduce intensity extremes.
     * Overlay blend the color onto the mask and composite the mask onto the base
     * image.
     *
     * @param baseImage
     * @param maskImage
     * @param surfaceColor
     * @throws Exception
     */
    private static void paintProcess( BufferedImage origImage, BufferedImage baseImage,
                                      BufferedImage maskImage, Color surfaceColor )
            throws Exception
    {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();

        // Getting the pixels once, since this is a fairly expensive process.
        // Also using PixelGrabber instead of getRGB() seems to be quicker.
        PixelGrabber pixelGrabber = new PixelGrabber( origImage, 0, 0, width, height, false );
        pixelGrabber.grabPixels();
        int[] imagePixels = (int[]) pixelGrabber.getPixels();

        PixelGrabber maskPixelGrabber = new PixelGrabber( maskImage, 0, 0, width, height, false );
        maskPixelGrabber.grabPixels();
        int[] maskPixels = (int[]) maskPixelGrabber.getPixels();

        // copy baseImage pixels into masked region and run analysis on bitmap data
        maskPixels = copyDesaturatedPixelsToMask( imagePixels, maskPixels, maskImage );

        // mitigate de-saturated region to reduce intensity extremes
        maskPixels = mitigationProcess( maskPixels, MITIGATED_MIN, MITIGATED_MAX );
        //maskImage.setRGB( 0, 0, width, height, maskPixels, 0, width );

        // use overlay blend mode to blend color back onto image
        // DE6714 - Rendering failing for some custom scenes, 400 response
        //          if mitigationProcess() returns a zero length maskPixel collection, do not apply this mask
        if ( maskPixels.length != 0 ) {
            blendOverlayWithMask( baseImage, maskPixels, surfaceColor );
        }
    }

    /**
     * Copy pixels from baseImage to maskImage in the masked region
     * only and de-saturate with same values as CVT
     * (red at 0.3, green at 0.6 and blue at 0.1).
     *
     * @param imagePixels - The image being analyzed
     * @param maskPixels - The mask of the image
     * @throws Exception
     */
    protected static int[] copyDesaturatedPixelsToMask( int[] imagePixels, int[] maskPixels,
                                                        BufferedImage maskImage )
            throws Exception
    {
        long startTime = System.currentTimeMillis();

        for( int i = 0; i < imagePixels.length; i++ )
        {
            // get RGB pixels
            int rgb = imagePixels[ i ];
            int blue = (rgb) & 0x000000FF;
            int green = (rgb >> 8) & 0x000000FF;
            int red = (rgb >> 16) & 0x000000FF;
            int mask = (maskPixels[ i ] >> 8) & 0x000000FF; // integer is in ABGR format rather than ARGB

            // de-saturate pixel
            int lum = (int) Math.round( 0.299 * red + 0.587 * green + 0.114 * blue );
            imagePixels[ i ] = (mask << 24) | (lum << 16) | (lum << 8) | lum;

        }

        if( log.isDebugEnabled() )
        {
            log.debug( "Time to destaurate: " + (System.currentTimeMillis() - startTime) + " ms" );
        }

        return imagePixels;
    }

    /**
     * Mitigate the image to reduce intensity extremes.
     *
     * @param imagePixels
     * @param mitigatedMin
     * @param mitigatedMax
     * @return
     * @throws Exception
     */
    protected static int[] mitigationProcess( int[] imagePixels, int mitigatedMin, int mitigatedMax )
            throws Exception
    {

        long startTime = System.currentTimeMillis();

        /*
         *  Calculate min/max/median intensities. Since we already have a grayscale image,
         *  the rgb value is just the intensity of one of the colors of that pixel
         */
        ArrayList<Integer> redValues = new ArrayList<Integer>();// new int[ imagePixels.length ];
        for( int pixel : imagePixels )
        {
            // check that pixel's alpha value is not 0 (not transparent)
            if( ((pixel >> 24) & 0x000000FF) != 0 )
            {
                // add the pixel's red value to the collection
                redValues.add( ((pixel >> 16) & 0x000000FF) );
            }
        }

        // DE6714 - Rendering failing for some custom scenes, 400 response
        //          if no red values were populated in the collection, this indicates the surface mask contains no pixels
        //          with any transparency. The mask will have no effect on the rendered image, thus processing of this
        //          specific mask can be aborted at this point and processing of additional masks (if they exist) can proceed
        if ( redValues.size() == 0 ) {
            return new int[] {};
        }

        Collections.sort( redValues );
        float avgIntensity = median( redValues );
        int minIntensity = redValues.get( 0 );
        int maxIntensity = redValues.get( redValues.size() - 1 );

        if( minIntensity == maxIntensity )
        {
            /*
             * The whole sample may be of the same color,
             * but we need some variation in order to make the transformation work,
             * so we will artificially introduce some.
             */
            if( minIntensity > 0 )
            {
                minIntensity-- ;
            }
            else if( maxIntensity < 0xff )
            {
                maxIntensity++ ;
            }

            // recalculate average.
            avgIntensity = (float) ((minIntensity + maxIntensity) / 2);
        }


        /*
         * Now that we have the intensity extremes, we can calculate the scale and offset of the color transformation.
         * The matrix represents a linear equation: f(i) = j,
         * where i is the initial grayscale image color in an arbitrary pixel
         * and j is the mitigated intensity.
         * The form of the equation is f(x) = s * x + t,
         * where s and t are found by linear algebra.
         *
         * If the range of intensities is not very much, that is, less than the mitigated range,
         * then we don't want to scale the image,
         * just "center" it closer to middle gray.
         * But the range of intensities is greater than the mitigated range,
         * we definitely want to scale down the range (hence, the "factor"),
         * and we also want to "center" them closer to middle gray (hence, the "offset").
         */
        float factor;
        float offset;

        float intensityRange = maxIntensity - minIntensity;
        int mitigatedRange = mitigatedMax - mitigatedMin;
        float middleGray = (float) ((mitigatedMin + mitigatedMax) / 2);

        if( intensityRange > mitigatedRange )
        {
            // the intensities range more than the mitigated range, so they need to be scaled down
            LinearEquationSolver solver = new LinearEquationSolver();
            solver.map( avgIntensity, middleGray );
            int aboveAverageRange = (int) (maxIntensity - avgIntensity);
            int underAverageRange = (int) (avgIntensity - minIntensity);
            if( underAverageRange < aboveAverageRange )
            {
                // average is skewed low, meaning higher values are outliers, so let them remain high
                solver.map( maxIntensity, Math.max( maxIntensity, mitigatedMax ) );
            }
            else
            {
                // average is skewed high, meaning lower values are outliers, so let them remain low
                solver.map( minIntensity, Math.min( minIntensity, mitigatedMin ) );
            }
            List<Float> coeff = solver.getCoefficients();
            factor = coeff.get( 0 );
            offset = coeff.get( 1 );
        }
        else
        {
            // The intensities range less than the mitigated range, so they should not be scaled down.
            // But the average intensity should correspond to the middle of the mitigated range.
            factor = 1.0f;
            offset = middleGray - avgIntensity;
        }

        // create an RGBA adjustment matrix
        double matrix[] = {
                factor, 0, 0, 0, offset, 0, factor, 0, 0, offset, 0, 0, factor, 0, offset, 0, 0, 0, 1,
                0 };

        // apply the color matrix adjustment
        imagePixels = colorMatrixFilter( imagePixels, matrix );

        if( log.isDebugEnabled() )
        {
            log.debug( "Time to mitigate: " + (System.currentTimeMillis() - startTime) + " ms" );
        }

        return imagePixels;
    }

    /**
     * Calculate median of sorted List<Integer>
     *
     * @param array Sorted List<Integer>
     * @return int Median value
     */
    protected static int median( List<Integer> list )
    {
        int median;
        if( list.size() % 2 == 0 )
        {
            median = (list.get( list.size() / 2 ) + list.get( list.size() / 2 - 1 )) / 2;
        }
        else
        {
            median = list.get( list.size() / 2 );
        }
        return median;
    }

    /**
     * Apply a 4 x 5 matrix transformation on the RGBA color and alpha values of every
     * pixel in the input image to produce a result with a new set of RGBA color and
     * alpha values. It allows saturation changes, hue rotation, luminance to alpha,
     * and various other effects.
     *
     * @param imagePixels
     * @param m
     * @return
     * @throws InterruptedException
     */
    protected static int[] colorMatrixFilter( int[] imagePixels, double[] m )
            throws InterruptedException
    {
        long startTime = System.currentTimeMillis();

        for( int i = 0; i < imagePixels.length; i++ )
        {
            // get RGBA values
            int rgb = imagePixels[ i ];
            int srcR = (rgb >> 16) & 0x000000FF;
            int srcG = (rgb >> 8) & 0x000000FF;
            int srcB = (rgb) & 0x000000FF;
            int srcA = ((rgb >> 24) & 0x000000FF);

            // skip calculations to save some cycles if the alpha of the pixel is 0
            if( srcA == 0 )
            {
                continue;
            }

            // apply matrix
            int redResult = (int) ((m[ 0 ] * srcR) + (m[ 1 ] * srcG) + (m[ 2 ] * srcB)
                    + (m[ 3 ] * srcA) + m[ 4 ]);
            int greenResult = (int) ((m[ 5 ] * srcR) + (m[ 6 ] * srcG) + (m[ 7 ] * srcB)
                    + (m[ 8 ] * srcA) + m[ 9 ]);
            int blueResult = (int) ((m[ 10 ] * srcR) + (m[ 11 ] * srcG) + (m[ 12 ] * srcB)
                    + (m[ 13 ] * srcA) + m[ 14 ]);
            int alphaResult = (int) ((m[ 15 ] * srcR) + (m[ 16 ] * srcG) + (m[ 17 ] * srcB)
                    + (m[ 18 ] * srcA) + m[ 19 ]);

            // set value to array of pixels
            imagePixels[ i ] = ((alphaResult & 0xFF) << 24) | ((redResult & 0xFF) << 16)
                    | ((greenResult & 0xFF) << 8) | (blueResult & 0xFF);
        }

        if( log.isDebugEnabled() )
        {
            log.debug( "Time to colorize: " + (System.currentTimeMillis() - startTime) + " ms" );
        }

        return imagePixels;
    }


    /**
     * Blend solid color onto image using a mask by performing an overlay blend mode composite
     * of surfaceColor onto maskImage and then an overlay blend mode composite of that
     * image onto baseImage.
     *
     * @param baseImage The image to be blended onto
     * @param maskImage Masked shape to blend the color with
     * @param surfaceColor Color to be blended
     * @throws Exception
     */
    protected static void blendOverlayWithMask( BufferedImage baseImage, int maskPixels[],
                                                Color surfaceColor )
            throws Exception
    {
        long startTime = System.currentTimeMillis();

        // create surface color pixel array
        int[] coloredPixels = new int[ maskPixels.length ];
        Arrays.fill( coloredPixels, surfaceColor.getRGB() );

        // perform the overlay blend
        blendOverlay( coloredPixels, maskPixels, baseImage );

        if( log.isDebugEnabled() )
        {
            log.debug( "Time to blendOverlayWithMask: "
                    + (System.currentTimeMillis() - startTime) + " ms" );
        }
    }

    /**
     * Takes an array of pixels and overlay blends them onto a destination
     * set of pixels. These overlay blended pixels are then written onto
     * baseImage.
     *
     * Definition of overlay blend:
     * Adjusts the color of each pixel based on the darkness of the background.
     * If the background is lighter than 50% gray, the display object and
     * background colors are screened, which results in a lighter color. If
     * the background is darker than 50% gray, the colors are multiplied,
     * which results in a darker color. Alpha is just passed through.
     *
     * @param src Source image pixels
     * @param dst Destination image pixels
     * @param baseImage Image to write pixels to
     * @return
     */
    protected static void blendOverlay( int[] src, int[] dst, BufferedImage baseImage )
    {
        int[] result = new int[ 4 ];
        int[] srcPixel = new int[ 4 ];
        int[] dstPixel = new int[ 4 ];

        float alphaValue = 1;

        for( int i = 0; i < src.length; i++ )
        {
            // pixels are stored as INT_ARGB
            // our arrays are [R, G, B, A]
            int pixel = src[ i ];
            srcPixel[ red ] = (pixel >> 16) & 0xFF;
            srcPixel[ green ] = (pixel >> 8) & 0xFF;
            srcPixel[ blue ] = (pixel) & 0xFF;
            srcPixel[ alpha ] = (pixel >> 24) & 0xFF;

            pixel = dst[ i ];
            dstPixel[ red ] = (pixel >> 16) & 0xFF;
            dstPixel[ green ] = (pixel >> 8) & 0xFF;
            dstPixel[ blue ] = (pixel) & 0xFF;
            dstPixel[ alpha ] = (pixel >> 24) & 0xFF;

            // skip calculations and writing of pixels to save some cycles
            // if the alpha of the pixel is 0
            if( dstPixel[ alpha ] == 0 )
            {
                continue;
            }

            // overlay calculation
            result[ red ] = dstPixel[ red ] < 128 ? dstPixel[ red ] * srcPixel[ red ] >> 7
                    : 255 - ((255 - dstPixel[ red ]) * (255 - srcPixel[ red ]) >> 7);
            result[ green ] = dstPixel[ green ] < 128 ? dstPixel[ green ] * srcPixel[ green ] >> 7
                    : 255 - ((255 - dstPixel[ green ]) * (255 - srcPixel[ green ]) >> 7);
            result[ blue ] = dstPixel[ blue ] < 128 ? dstPixel[ blue ] * srcPixel[ blue ] >> 7
                    : 255 - ((255 - dstPixel[ blue ]) * (255 - srcPixel[ blue ]) >> 7);
            result[ alpha ] = dstPixel[ alpha ];

            // mixes the result with the opacity
            pixel = ((int) (dstPixel[ 3 ] + (result[ 3 ] - dstPixel[ 3 ]) * alphaValue) & 0xFF) << 24
                    | ((int) (dstPixel[ 0 ] + (result[ 0 ] - dstPixel[ 0 ]) * alphaValue) & 0xFF) << 16
                    | ((int) (dstPixel[ 1 ] + (result[ 1 ] - dstPixel[ 1 ]) * alphaValue) & 0xFF) << 8
                    | (int) (dstPixel[ 2 ] + (result[ 2 ] - dstPixel[ 2 ]) * alphaValue) & 0xFF;

            // write pixel on to base image as this is our final step
            baseImage.setRGB( (i % baseImage.getWidth()), (i / baseImage.getWidth()), pixel );
        }
    }


}

