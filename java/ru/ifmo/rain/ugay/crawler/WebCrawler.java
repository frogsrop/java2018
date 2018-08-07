package ru.ifmo.rain.ugay.crawler;

import com.sun.deploy.security.BlockedException;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import javafx.util.Pair;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class WebCrawler implements Crawler {

    private final int perHost;
    private Downloader downloader;
    private Thread[] downloadersTh;
    private Thread[] workersTh;
    private BlockingQueue<Pair<Document, Integer>> sites;
    private BlockingQueue<Pair<String, Integer>> toDownload;
    private BlockingQueue<String> ans;

    private class Extractor extends Thread {
        public void run() {
            Pair<Document, Integer> r;
            try {
                while (true) {
                    try {
                        r = sites.take();
                        System.out.println(r.getKey());
                        System.out.flush();
                        if (r.getValue() <= 1) {
                            try {
                                for (String st : r.getKey().extractLinks()) {
                                    try {
                                        toDownload.put(new Pair<>(st, r.getValue() + 1));
                                    } catch (InterruptedException ex) {
                                        break;
                                    }
                                }
                            } catch (IOException ignored) {
                                break;
                            }
                        } else {
                            System.out.println("??");
                            System.out.flush();
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            } catch (BlockedException ex) {
                System.err.println("suk");
            }
        }
    }

    private class DocumentDownloader extends Thread {
        public void run() {
            Pair<String, Integer> r;
            while (true) {
                try {
                    try {
                        r = toDownload.take();
                        ans.put(r.getKey());
                        try {
                            Document kids = downloader.download(r.getKey());
                            try {
                                sites.put(new Pair<>(kids, r.getValue()));
                            } catch (InterruptedException ex) {
                                break;
                            }
                        } catch (IOException ignored) {

                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                } catch (BlockedException ex) {
                    System.err.println("suk");
                    break;
                }
            }
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloadersTh = new Thread[downloaders];
        workersTh = new Thread[extractors];
        for (int i = 0; i < downloadersTh.length; i++) {
            downloadersTh[i] = new DocumentDownloader();
            downloadersTh[i].start();
        }
        for (int i = 0; i < workersTh.length; i++) {
            workersTh[i] = new Extractor();
            workersTh[i].start();
        }
        ans = new LinkedBlockingQueue<>();
        sites = new LinkedBlockingQueue<>();
        toDownload = new LinkedBlockingQueue<>();
        this.perHost = perHost;
    }

    @Override
    public Result download(String url, int depth) {
        try {
            toDownload.put(new Pair<>(url, 0));
        } catch (InterruptedException ignored) {

        }
        int x = 10000;
        while (x > 0) {
            if (ans.size() > 0) {
                for (String ps : ans) {
                    System.out.println(ps);
                }
                System.out.println();
                System.out.flush();
            }
        }
        return null;
    }

    @Override
    public void close() {
        for (Thread aDownloadersTh : downloadersTh) {
            aDownloadersTh.interrupt();
        }
        for (Thread aWorkersTh : workersTh) {
            aWorkersTh.interrupt();
        }
        try {
            for (Thread aDownloadersTh : downloadersTh) {
                aDownloadersTh.join();
            }
            for (Thread aWorkersTh : workersTh) {
                aWorkersTh.join();
            }
        } catch (InterruptedException ignored) {
        }
    }
}
