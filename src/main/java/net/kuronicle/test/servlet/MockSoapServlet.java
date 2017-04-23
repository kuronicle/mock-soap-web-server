package net.kuronicle.test.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

@Slf4j
public class MockSoapServlet extends HttpServlet {

    private static final long serialVersionUID = -4435776387872183605L;

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Setter
    private String dataStoreBaseDir = "/tmp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Set base dir. dir= {}", dataStoreBaseDir);
        String baseDirPath = FilenameUtils.concat(dataStoreBaseDir, req.getPathInfo().substring(1));
        log.debug("Set data store dir. dir={}", baseDirPath);

        storeRequestMessage(baseDirPath, req);
        returnResponse(baseDirPath, resp);
    }

    private void returnResponse(String baseDirPath, HttpServletResponse resp) throws IOException {
        // TODO Auto-generated method stub
        File responseFile = null;

        String sequentialResponseDirPath = FilenameUtils.concat(baseDirPath, "response/sequential");
        File sequentialResponseDir = new File(sequentialResponseDirPath);
        if (sequentialResponseDir.exists() && sequentialResponseDir.isDirectory()) {
            File[] sequentialResponseFiles = sequentialResponseDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile()) {
                        return true;
                    }
                    return false;
                }
            });
            Arrays.sort(sequentialResponseFiles, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName());
                }
            });
            if (sequentialResponseFiles != null && sequentialResponseFiles.length > 0) {
                String responseFilePath = FilenameUtils.concat(sequentialResponseDirPath, "old/" + sequentialResponseFiles[0].getName());
                responseFile = new File(responseFilePath);
                FileUtils.moveFile(sequentialResponseFiles[0], responseFile);
                log.info("Read response data. file={}", responseFile.getAbsolutePath());
            }
        }

        if (responseFile == null) {
            String defaultResponseFilePath = FilenameUtils.concat(baseDirPath, "response/default.xml");
            File defaultResponseFile = new File(defaultResponseFilePath);
            if (defaultResponseFile.isFile()) {
                responseFile = defaultResponseFile;
                log.info("Read response data. file={}", responseFile.getAbsolutePath());
            }
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (responseFile != null && responseFile.isFile()) {
                inputStream = new FileInputStream(responseFile);
            } else {
                String message = String.format("Error! Please set response files to %s or %s", FilenameUtils.concat(baseDirPath, "response/default.xml"),
                        FilenameUtils.concat(baseDirPath, "response/sequential/xxx.xml"));
                inputStream = new ByteArrayInputStream(message.getBytes());
            }
            outputStream = resp.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

    }

    private void storeRequestMessage(String baseDirPath, HttpServletRequest req) {

        String storeDirPath = FilenameUtils.concat(baseDirPath, "request");
        File storeDir = new File(storeDirPath);
        if (!storeDir.exists()) {
            storeDir.mkdirs();
            log.info("Create request store dir. dir={}", storeDir.getAbsolutePath());
        }

        String storeFileName = "request_" + getNowAsString() + ".xml";
        String storePath = FilenameUtils.concat(storeDirPath, storeFileName);
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            inputStream = req.getInputStream();
            fileOutputStream = new FileOutputStream(storePath);
            log.info("Store request. file={}", storePath);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(inputStream);
        }
    }

    private String getNowAsString() {
        synchronized (dateFormat) {
            return dateFormat.format(new Date());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
