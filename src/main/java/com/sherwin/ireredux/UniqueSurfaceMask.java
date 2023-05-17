package com.sherwin.ireredux;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a collection of surface names that all have
 * the same associated masking data. The isUsed boolean is
 * set to TRUE if the masking data has already been applied
 * to the base image. The mask value is the same for all
 * the surfaces in the surfaceNames list.
 */
public class UniqueSurfaceMask
{
    private boolean used;
    private List<String> surfaceNames = new ArrayList<String>();
    private String mask;

    public UniqueSurfaceMask( String aMask, String surfaceName )
    {
        mask = aMask;
        surfaceNames.add( surfaceName );
    }

    public boolean isUsed()
    {
        return used;
    }

    public void setIsUsed( boolean isUsed )
    {
        this.used = isUsed;
    }

    public void addSurfaceName( String surfaceName )
    {
        surfaceNames.add( surfaceName );
    }

    public boolean contains( String surfaceName )
    {
        return surfaceNames.contains( surfaceName );
    }

    public List<String> getSurfaceNames()
    {
        return surfaceNames;
    }

    public String getMask()
    {
        return mask;
    }

    public void setMask( String mask )
    {
        this.mask = mask;
    }

}

