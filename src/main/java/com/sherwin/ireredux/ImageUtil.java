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

import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;

/**
 * This class hold utility methods related to image manipulation.
 */
public class ImageUtil
{

    /**
     * Return a new image based on the baseImage parameter that has been
     * resized to the given parameters.
     *
     * @param baseImage - The user's image
     * @param targetWidth - The resize width
     * @param targetHeight - The resize height
     * @return a resized {@link BufferedImage}
     */
    public static BufferedImage resizeImage( BufferedImage baseImage, int targetWidth,
                                             int targetHeight )
    {
        /*
         * Return the original image as-is if both targetWidth and targetHeight are 0 or if the
         * original image dimensions are the same as the target ones
         */
        if( (baseImage == null) || (targetWidth == 0 && targetHeight == 0)
                || (baseImage.getWidth() == targetWidth && baseImage.getHeight() == targetHeight) )
        {
            return baseImage;
        }

        // convert width and height to double for calculations
        double imageWidth = (double) baseImage.getWidth();
        double imageHeight = (double) baseImage.getHeight();

        /*
         * Create an image according to the width and height parameters. These
         * calculations are needed to retain backward compatibility with earlier
         * versions of ColorSnap Studio that are expecting certain image sizing
         * behaviors based on how Swatchbox was working.
         */
        BufferedImage resizedImage = null;
        if( targetWidth > 0 && targetHeight > 0 )
        {
            // calculate minimum dimension value
            double Wr = targetWidth / imageWidth;
            double Hr = targetHeight / imageHeight;
            double Mr = Math.min( Wr, Hr );

            // calculate new image dimensions
            double Wx = Mr * imageWidth;
            double Hx = Mr * imageHeight;

            // calculate absolute distance from image dimension to target dimension
            double Wd = Math.abs( Wx - targetWidth );
            double Hd = Math.abs( Hx - targetHeight );

            // resize image to fit the smaller of the requested dimensions
            if( Hd == Wd )
            {
                if( targetHeight < targetWidth )
                {
                    resizedImage = Scalr.resize( baseImage, Scalr.Mode.FIT_TO_HEIGHT, targetWidth,
                            targetHeight );
                }
                else
                {
                    resizedImage = Scalr.resize( baseImage, Scalr.Mode.FIT_TO_WIDTH, targetWidth,
                            targetHeight );
                }
            }
            else if( Hd < Wd )
            {
                resizedImage = Scalr.resize( baseImage, Scalr.Mode.FIT_TO_HEIGHT, targetWidth,
                        targetHeight );
            }
            else
            {
                resizedImage = Scalr.resize( baseImage, Scalr.Mode.FIT_TO_WIDTH, targetWidth,
                        targetHeight );
            }

        }
        else if( targetWidth > 0 )
        {
            resizedImage = Scalr.resize( baseImage, targetWidth );
        }
        else if( targetHeight > 0 )
        {
            resizedImage = Scalr.resize( baseImage, targetHeight );
        }
        else
        {
            //no width or height parameters available - return full-sized image
            resizedImage = baseImage;
        }
        return resizedImage;
    }

}

