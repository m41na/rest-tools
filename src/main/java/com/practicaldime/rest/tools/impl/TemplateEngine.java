package com.practicaldime.rest.tools.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import com.practicaldime.rest.tools.api.ApiReqComparator;
import com.practicaldime.rest.tools.model.ApiReq;
import com.practicaldime.rest.tools.util.RestToolsJson;

public class TemplateEngine {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateEngine.class);

    private static TemplateEngine instance;
    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    private final int SIZE = 2048;

    private TemplateEngine() {
        cfg.setClassForTemplateLoading(getClass(), "/rest");

        // Some other recommended settings:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public static TemplateEngine getInstance() {
        if (instance == null) {
            instance = new TemplateEngine();
        }
        return instance;
    }

    public Configuration config() {
        return this.cfg;
    }

    public String readResourceFile(String location) throws IOException {
        byte[] headersBytes = new byte[SIZE];
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(location)) {
            int length = inputStream.read(headersBytes, 0, SIZE);
            return new String(headersBytes, 0, length);
        }
    }

    public void writeToRemoteFile(StringBuilder content) throws IOException {
        try (FileWriter output = new FileWriter(new File(getRemoteFilePath()))) {
            output.write(content.toString());
        } catch (Exception e) {
            LOG.error(String.format("%s was NOT able to write to the remote repository. Reason: %s", TemplateEngine.class.getSimpleName(), e.getMessage()));
        }
    }

    public String getRemoteFilePath() {
        return "E:\\temp\\rest-api\\generated-raml.html";
    }

    public String mergeWithTemplates(Collection<ApiReq> endpoints, Boolean writeToRemoteFile) throws Exception {
        StringBuilder apiDocBuilder = new StringBuilder();
        apiDocBuilder.append(readResourceFile("rest/headers-template.txt"));

        List<ApiReq> sortedList = new ArrayList<>(endpoints);
        Collections.sort(sortedList, new ApiReqComparator());
        for (ApiReq endpoint : sortedList) {
            Template template = TemplateEngine.getInstance().config().getTemplate("endpoint-template.txt");
            //Writer consoleWriter = new OutputStreamWriter(System.out);
            Writer endpointWriter = new StringWriter();
            template.process(endpoint, endpointWriter);
            apiDocBuilder.append(endpointWriter.toString());
            System.out.printf("merging endpoint %s with template\n", endpoint.toString());
        }

        if (writeToRemoteFile) {
            writeToRemoteFile(apiDocBuilder);
        }

        //return generated RAML
        return apiDocBuilder.toString();
    }

    public static void main(String... args) throws Exception {
        int SIZE = 2048;
        byte[] responseBytes = new byte[SIZE];
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("rest/sample-json-response.txt")) {
            int length = inputStream.read(responseBytes, 0, SIZE);
            String sampleResponse = new String(responseBytes, 0, length);

            ApiReq endpoint = RestToolsJson.fromJson(sampleResponse, ApiReq.class);
            System.out.println(new TemplateEngine().mergeWithTemplates(Arrays.asList(endpoint), false));
        }
    }
}
