// JoalAudioBuffer.java

package jmri.jmrit.audio;

import java.nio.ByteBuffer;
import jmri.util.FileUtil;
import net.java.games.joal.AL;
import net.java.games.joal.ALException;
import net.java.games.joal.util.ALut;

/**
 * JOAL implementation of the Audio Buffer sub-class.
 * <P>
 * For now, no system-specific implementations are forseen - this will remain
 * internal-only
 * <br><br><hr><br><b>
 *    This software is based on or using the JOAL Library available from
 *    http://joal.dev.java.net/
 * </b><br><br>
 *    JOAL License:
 * <br><i>
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <br>
 * -Redistribution of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <br>
 * -Redistribution in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <br>
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <br>
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * <br>
 * You acknowledge that this software is not designed or intended for use in the
 * design, construction, operation or maintenance of any nuclear facility.
 * <br><br><br></i>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision: 1.4 $
 */
public class JoalAudioBuffer extends AbstractAudioBuffer {

    private static AL al = JoalAudioFactory.getAL();

    // Arrays to hold various .wav file information
    private int[] _format = new int[1];
    private int[] _dataStorageBuffer = new int[1];
    private ByteBuffer[] _data = new ByteBuffer[1];
    private int[] _size = new int[1];
    private int[] _freq = new int[1];
    private int[] _loop = new int[1];

    private boolean _initialised = false;

    /**
     * Constructor for new JoalAudioBuffer with system name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     */
    public JoalAudioBuffer(String systemName) {
        super(systemName);
        if (log.isDebugEnabled()) log.debug("New JoalAudioBuffer: "+systemName);
        _initialised = init();
    }

    /**
     * Constructor for new JoalAudioBuffer with system name and user name
     *
     * @param systemName AudioBuffer object system name (e.g. IAB4)
     * @param userName AudioBuffer object user name
     */
    public JoalAudioBuffer(String systemName, String userName) {
        super(systemName, userName);
        if (log.isDebugEnabled()) log.debug("New JoalAudioBuffer: "+userName+" ("+systemName+")");
        _initialised = init();
    }

    /**
     * Initialise this JoalAudioBuffer.
     *
     * @return true, if initialisation successful
     */
    private boolean init() {
        // Try to create an empty buffer that will hold the actual sound data
        al.alGenBuffers(1, _dataStorageBuffer, 0);
        if (JoalAudioFactory.checkALEError()) {
            log.warn("Error creating JoalAudioBuffer (" + this.getSystemName() + ")");
            return false;
        }
        
        this.setState(STATE_EMPTY);
        return true;

    }

    /**
     * Return reference to the DataStorageBuffer integer array
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Buffer
     * </u>
     * @return buffer[] reference to DataStorageBuffer
     */
    protected int[] getDataStorageBuffer() {
        return _dataStorageBuffer;
    }
    
    @Override
    public String toString() {
        if (this.getState()!=STATE_LOADED) {
            return "Empty buffer";
        } else {
            return this.getURL() + " (" + parseFormat() + ", " + this._freq[0] + " Hz)";
        }
    }

    /**
     * Internal method to return a string representation of the audio format
     * @return string representation
     */
    private String parseFormat() {
        switch (this._format[0]) {
            case AL.AL_FORMAT_MONO8:
                return "8-bit mono";
            case AL.AL_FORMAT_MONO16:
                return "16-bit mono";
            case AL.AL_FORMAT_STEREO8:
                return "8-bit stereo";
            case AL.AL_FORMAT_STEREO16:
                return "16-bit stereo";
        }
        if (this._format[0]==JoalAudioFactory.AL_FORMAT_QUAD8
                && JoalAudioFactory.AL_FORMAT_QUAD8!=FORMAT_UNKNOWN)
            return "8-bit quadrophonic";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_QUAD16
                && JoalAudioFactory.AL_FORMAT_QUAD16!=FORMAT_UNKNOWN)
            return "16-bit quadrophonic";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_51CHN8
                && JoalAudioFactory.AL_FORMAT_51CHN8!=FORMAT_UNKNOWN)
            return "8-bit 5.1 surround";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_51CHN16
                && JoalAudioFactory.AL_FORMAT_51CHN16!=FORMAT_UNKNOWN)
            return "16-bit 5.1 surround";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_61CHN8
                && JoalAudioFactory.AL_FORMAT_61CHN8!=FORMAT_UNKNOWN)
            return "8-bit 6.1 surround";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_61CHN16
                && JoalAudioFactory.AL_FORMAT_61CHN16!=FORMAT_UNKNOWN)
            return "16-bit 6.1 surround";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_71CHN8
                && JoalAudioFactory.AL_FORMAT_71CHN8!=FORMAT_UNKNOWN)
            return "8 bit 7.1 surround";
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_71CHN16
                && JoalAudioFactory.AL_FORMAT_71CHN16!=FORMAT_UNKNOWN)
            return "16 bit 7.1 surround";
        else
            return "unknown format";
    }

    protected boolean loadBuffer() {
        if (!_initialised) {
            return false;
        }
        // Reset buffer state
        // Use super class methods to postpone loop buffer generation
        super.setStartLoopPoint(0);
        super.setEndLoopPoint(0);
        this.setState(STATE_EMPTY);

        // Load the specified .wav file into data arrays
        try {
            ALut.alutLoadWAVFile(FileUtil.getExternalFilename(this.getURL())
                    , _format, _data, _size, _freq, _loop);
        }
        catch (ALException e) {
            log.warn("Error loading JoalAudioBuffer: " + e.getMessage());
            return false;
        }

        // Store the actual data in the buffer
        al.alBufferData(_dataStorageBuffer[0], _format[0], _data[0], _size[0], _freq[0]);
        this.setState(STATE_LOADED);

        // Set initial loop points
        // Use super class methods to postpone loop buffer generation
        super.setStartLoopPoint(0);
        super.setEndLoopPoint(_size[0]);
        generateLoopBuffers();

        if (log.isDebugEnabled()) {
            log.debug("Loaded buffer: " + this.getSystemName());
            log.debug(" from file: " + this.getURL());
            log.debug(" format: " + parseFormat() + ", " + _freq[0] + " Hz");
            log.debug(" length: " + _size[0]);
        }
        return true;
    }

    // Override super class method to ensure loop buffers are re-generated
    @Override
    public void setStartLoopPoint(long startLoopPoint) {
        super.setStartLoopPoint(startLoopPoint);
        generateLoopBuffers();
    }

    // Override super class method to ensure loop buffers are re-generated
    @Override
    public void setEndLoopPoint(long endLoopPoint) {
        super.setEndLoopPoint(endLoopPoint);
        generateLoopBuffers();
    }

    /**
     * Method used to generate any necessary loop buffers.
     */
    protected void generateLoopBuffers() {
        // TODO: Actually write this bit ;-)
        if (log.isDebugEnabled()) {
            log.debug("Method generateLoopBuffers() called for buffer " + this.getSystemName());
        }
    }

    public int getFormat() {
        switch (this._format[0]) {
            case AL.AL_FORMAT_MONO8:
                return FORMAT_8BIT_MONO;
            case AL.AL_FORMAT_MONO16:
                return FORMAT_16BIT_MONO;
            case AL.AL_FORMAT_STEREO8:
                return FORMAT_8BIT_STEREO;
            case AL.AL_FORMAT_STEREO16:
                return FORMAT_16BIT_STEREO;
        }
        if (this._format[0]==JoalAudioFactory.AL_FORMAT_QUAD8
                && JoalAudioFactory.AL_FORMAT_QUAD8!=FORMAT_UNKNOWN)
            return FORMAT_8BIT_QUAD;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_QUAD16
                && JoalAudioFactory.AL_FORMAT_QUAD16!=FORMAT_UNKNOWN)
            return FORMAT_16BIT_QUAD;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_51CHN8
                && JoalAudioFactory.AL_FORMAT_51CHN8!=FORMAT_UNKNOWN)
            return FORMAT_8BIT_5DOT1;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_51CHN16
                && JoalAudioFactory.AL_FORMAT_51CHN16!=FORMAT_UNKNOWN)
            return FORMAT_16BIT_5DOT1;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_61CHN8
                && JoalAudioFactory.AL_FORMAT_61CHN8!=FORMAT_UNKNOWN)
            return FORMAT_8BIT_6DOT1;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_61CHN16
                && JoalAudioFactory.AL_FORMAT_61CHN16!=FORMAT_UNKNOWN)
            return FORMAT_16BIT_6DOT1;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_71CHN8
                && JoalAudioFactory.AL_FORMAT_71CHN8!=FORMAT_UNKNOWN)
            return FORMAT_8BIT_7DOT1;
        else if (this._format[0]==JoalAudioFactory.AL_FORMAT_71CHN16
                && JoalAudioFactory.AL_FORMAT_71CHN16!=FORMAT_UNKNOWN)
            return FORMAT_16BIT_7DOT1;
        else
            return FORMAT_UNKNOWN;
    }

    protected void cleanUp() {
        if (_initialised) {
            al.alDeleteBuffers(1, _dataStorageBuffer, 0);
        }
        if (log.isDebugEnabled()) log.debug("Cleanup JoalAudioBuffer (" + this.getSystemName() + ")");
        this.dispose();
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JoalAudioBuffer.class.getName());

}

/* $(#)JoalAudioBuffer.java */