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

/**
 * Constants specific to the Image Rendering Engine application
 *
 */
public class ImageRenderingConstants
{
    /*
     * Request parameter values
     */
    public static final String BASE_IMAGE_LOCATION_PARAMETER = "i";
    public static final String RENDERED_IMAGE_OUTPUT_PARAMETER = "o";
    public static final String RENDERED_IMAGE_HEIGHT = "h";
    public static final String RENDERED_IMAGE_WIDTH = "w";
    public static final String SURFACE_DATA_DELIMITER = "~";

    /*
     * Response type - if set to 'text' then an image will not be returned in
     * the servlet response and instead a text-based response with information
     * about the request and processing will be returned. Intended for testing only.
     */
    public static final String RESPONSE_TYPE = "response";
    public static final String TEXT_RESPONSE = "text/plain";
    public static final String IMAGE_JPEG_RESPONSE = "image/jpeg";

    /*
     * Mitigated limit values - same as CVT
     */
    public static final int MITIGATED_MIN = 0x40;
    public static final int MITIGATED_MAX = 0xc0;

    /*
     * Security
     */
    public static final String JAXP_XXE_FEATURE_DOC_TYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    public static final String JAXP_XXE_FEATURE_EXT_GEN_ENTITY = "http://xml.org/sax/features/external-general-entities";
    public static final String JAXP_XXE_FEATURE_EXT_PARAM_ENTITY = "http://xml.org/sax/features/external-parameter-entities";
    public static final String JAXP_XXE_FEATURE_EXT_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

}
