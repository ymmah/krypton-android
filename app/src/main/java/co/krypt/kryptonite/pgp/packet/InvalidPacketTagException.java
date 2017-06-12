package co.krypt.kryptonite.pgp.packet;

import co.krypt.kryptonite.pgp.PGPException;

/**
 * Created by Kevin King on 6/12/17.
 * Copyright 2016. KryptCo, Inc.
 */

public class InvalidPacketTagException extends PGPException {
    InvalidPacketTagException(String message) {
        super(message);
    }
    public InvalidPacketTagException() {
        super();
    }
}
