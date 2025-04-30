package com.example.sourcesearchtool_practice.util;

import org.springframework.stereotype.Component;

@Component
public class IndexingCheck {
    private boolean isIndexing = false;

    public synchronized void start() {
        this.isIndexing = true;
    }

    public synchronized void finish() {
        this.isIndexing = false;
    }

    public boolean isIndexing() {
        return isIndexing;
    }
}
