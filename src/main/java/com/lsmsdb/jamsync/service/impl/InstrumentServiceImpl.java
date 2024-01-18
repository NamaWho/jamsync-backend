package com.lsmsdb.jamsync.service.impl;

import com.lsmsdb.jamsync.service.InstrumentService;

import java.util.Arrays;
import java.util.List;

public class InstrumentServiceImpl implements InstrumentService {
    private static final List<String> ALLOWED_INSTRUMENTS =
            Arrays.asList(
                    "Guitar",
                    "Piano",
                    "Drums"
            );

    @Override
    public List<String> getInstruments() {
        return ALLOWED_INSTRUMENTS;
    }
}
