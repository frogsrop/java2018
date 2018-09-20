package ru.ifmo.rain.ugay.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private BlockingQueue<Runnable> toDownload;
    private BlockingQueue<Runnable> toExtract;
    private ThreadPoolExecutor downloadExecutor;
    private List<String> stringList;
    private Map<String, IOException> exceptionMap;
    private Set<String> downloadedSet;
    private int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        toDownload = new LinkedBlockingQueue<>();
        toExtract = new LinkedBlockingQueue<>();
        downloadExecutor = new ThreadPoolExecutor(0, downloaders, 1000, TimeUnit.MILLISECONDS, toDownload);
        downloadExecutor.prestartAllCoreThreads();
        stringList = new ArrayList<>();
        exceptionMap = new TreeMap<>();
        downloadedSet = new HashSet<>();
        this.downloader = downloader;
        this.perHost = perHost;
    }

    class downloadRunnable implements Runnable {
        int deepness;
        String url;

        downloadRunnable(String url1, int deepness1) {
            url = url1;
            deepness = deepness1;
        }

        @Override
        public void run() {
            try {
                Document downloadedDocument = downloader.download(url);
//                System.err.println("adding to list");
                synchronized (stringList) {
                    stringList.add(url);
                }
//                System.err.println("added. Current size: " + stringList.size());
                addToExtract(downloadedDocument, deepness - 1);
            } catch (IOException e) {
                synchronized (exceptionMap) {
                    exceptionMap.put(url, e);
                }
            }
        }
    }

    class extractRunnable implements Runnable {
        int deepness;
        Document document;

        extractRunnable(Document document1, int deepness1) {
            document = document1;
            deepness = deepness1;
        }

        @Override
        public void run() {

        }
    }

    private void addToDownload(String s, int deepness) {
//        System.err.println("added: " + s);
        if (deepness == 0) {
            return;
        }
        if (downloadedSet.add(s)) {
            downloadExecutor.execute(new downloadRunnable(s, deepness));
        }
    }

    private void addToExtract(Document d, int deepness) {
        try {
            toExtract.put(new extractRunnable(d, deepness));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void takeExtract() {
        while (!toExtract.isEmpty()) {
            try {
                try {
                    Runnable r = toExtract.take();
                    List<String> extracted = ((extractRunnable) r).document.extractLinks();
                    int deepness = ((extractRunnable) r).deepness;
                    for (String url : extracted) {
                        addToDownload(url, deepness);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public Result download(String s, int i) {
        addToDownload(s, i);
        do {
            while (toExtract.isEmpty()) {
            }
            takeExtract();
        } while (toDownload.size() > 0 || toExtract.size() > 0);
        return new Result(stringList, exceptionMap);
    }

    @Override
    public void close() {
        downloadExecutor.shutdown();
    }

    public static void main(String[] args) {
        try {
            WebCrawler wc = new WebCrawler(new CachingDownloader(), 10000, 10000, 100);
            Result r = wc.download("http://neerc.ifmo.ru/~sta/2017-2018/2-discrete-math/", 2);
            for (String s : r.getDownloaded()) {
                System.out.println(s);
            }
            wc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
