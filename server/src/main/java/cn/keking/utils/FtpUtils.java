package cn.keking.utils;

import cn.keking.config.ConfigConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @auther: chenjh
 * @since: 2019/6/18 14:36
 */
public class FtpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpUtils.class);

    public static FTPClient connect(FtpConnectionInfo connectionInfo) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(connectionInfo.getHost(), connectionInfo.getPort());
        if (!StringUtils.isEmpty(connectionInfo.getUsername()) && !StringUtils.isEmpty(connectionInfo.getPassword())) {
            ftpClient.login(connectionInfo.getUsername(), connectionInfo.getPassword());
        }
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
        }
        ftpClient.setControlEncoding(connectionInfo.getControlEncoding());
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        return ftpClient;
    }

    public static void download(String ftpUrl, String localFilePath, String ftpUsername, String ftpPassword, String ftpControlEncoding) throws IOException {
        String username = StringUtils.isEmpty(ftpUsername) ? ConfigConstants.getFtpUsername() : ftpUsername;
        String password = StringUtils.isEmpty(ftpPassword) ? ConfigConstants.getFtpPassword() : ftpPassword;
        String controlEncoding = StringUtils.isEmpty(ftpControlEncoding) ? ConfigConstants.getFtpControlEncoding() : ftpControlEncoding;
        URL url = new URL(ftpUrl);
        String host = url.getHost();
        int port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
        String remoteFilePath = url.getPath();
        LOGGER.debug("FTP connection url:{}, username:{}, password:{}, controlEncoding:{}, localFilePath:{}", ftpUrl, username, password, controlEncoding, localFilePath);
        FtpConnectionInfo connectionInfo = new FtpConnectionInfo(host, port, username, password, controlEncoding);
        FTPClient ftpClient = connect(connectionInfo);
        OutputStream outputStream = Files.newOutputStream(Paths.get(localFilePath));
        ftpClient.enterLocalPassiveMode();
        boolean downloadResult = ftpClient.retrieveFile(new String(remoteFilePath.getBytes(controlEncoding), StandardCharsets.ISO_8859_1), outputStream);
        LOGGER.debug("FTP download result {}", downloadResult);
        outputStream.flush();
        outputStream.close();
        ftpClient.logout();
        ftpClient.disconnect();
    }
}

class FtpConnectionInfo {
    private String host;
    private int port;
    private String username;
    private String password;
    private String controlEncoding;

    public FtpConnectionInfo(String host, int port, String username, String password, String controlEncoding) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.controlEncoding = controlEncoding;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getControlEncoding() {
        return controlEncoding;
    }
}
