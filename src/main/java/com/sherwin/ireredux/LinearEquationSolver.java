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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to calculate values used during the
 * mitigation of color data.
 */
public class LinearEquationSolver
{
    ArrayList<List<Float>> points;

    /**
     * Sole constructor
     */
    public LinearEquationSolver()
    {
        points = new ArrayList<List<Float>>();
    }

    /**
     * Map the input to the output by the transformation
     * defined by the linear equation with the coefficients to be calculated.
     *
     * @param input
     * @param output
     */
    public void map( float input, float output )
            throws Exception
    {
        if( points.size() >= 2 )
        {
            throw new Exception( "mapping too many points" );
        }
        List<Float> point = new ArrayList<Float>();
        point.add( input );
        point.add( output );
        points.add( point );
    }

    /**
     * Calculates the coefficients for a linear equation
     * of the form s*x + t so that f(x) = s*x + t is a function
     * such that f(a) = b and f(c) = d for the mapped points (a, b), (c, d)
     *
     * @return a collection of coefficients as Floats
     */
    public List<Float> getCoefficients()
            throws Exception
    {
        if( points.size() < 2 )
        {
            throw new Exception( "too few points" );
        }

        Float a = points.get( 0 ).get( 0 );
        Float b = points.get( 0 ).get( 1 );
        Float c = points.get( 1 ).get( 0 );
        Float d = points.get( 1 ).get( 1 );

        if( a == c )
        {
            throw new Exception( "same input maps to multiple outputs" );
        }

        Float s = (b - d) / (a - c);
        Float t = b - s * a;

        List<Float> coeff = new ArrayList<Float>();
        coeff.add( s );
        coeff.add( t );

        return coeff;
    }
}

