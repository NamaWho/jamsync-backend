package com.lsmsdb.jamsync.controller;

import com.lsmsdb.jamsync.service.InstrumentService;
import com.lsmsdb.jamsync.service.factory.InstrumentServiceFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private InstrumentService instrumentService;

    public InstrumentController() { this.instrumentService = InstrumentServiceFactory.create().getService(); }

    @GetMapping("/")
    public List<String> getInstrument() {
        return instrumentService.getInstruments();
    }
}
