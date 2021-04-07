package xyz.xieguoking;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static xyz.xieguoking.Utils.getStackTrace;

/**
 * @author xieguoking
 * @author (2021 / 4 / 7 add by xieguoking
 * @version 1.0
 * @since 1.0
 */
public class Uploader {

    private Logger logger = Logger.getLogger(Uploader.class.getName());
    private final File root;
    private final String repositoryUrl;
    private final String repositoryId;

    public static final String FORMAT = "${mvn} mvn deploy:deploy-file " +
            "-DgroupId=${groupId} " +
            "-DartifactId=${artifactId} " +
            "-Dversion=${version} " +
            "-Dpackaging=jar " +
            "-Dfile=${jarFile} " +
            "-DpomFile=${pomFile} " +
            "-Durl=${repositoryUrl} " +
            "-DrepositoryId=${repositoryId} ";

    public Uploader(File root, String repositoryUrl, String repositoryId) {
        this.root = root;
        this.repositoryUrl = repositoryUrl;
        this.repositoryId = repositoryId;
    }

    public void uploadAll() {
        final File[] files = root.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                upload(file);
            }
        }
    }

    public void upload(File jarFile) {
        try {
            ZipFile zipFile = new ZipFile(jarFile);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith("/pom.xml")) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        byte[] data = new byte[(int) entry.getSize()];
                        inputStream.read(data);
                        File pomFile = new File(jarFile.getParent(), "pom.xml");
                        try (OutputStream os = new FileOutputStream(pomFile)) {
                            os.write(data);
                        }
                        PomInfo pomInfo = parsePomInfo(data);

                        String command = FORMAT
                                .replace("${repositoryUrl}", repositoryUrl)
                                .replace("${repositoryId}", repositoryId)
                                .replace("${groupId}", pomInfo.getGroupId())
                                .replace("${artifactId}", pomInfo.getArtifactId())
                                .replace("${version}", pomInfo.getVersion())
                                .replace("${jarFile}", jarFile.getAbsolutePath())
                                .replace("${pomFile}", pomFile.getAbsolutePath());
                        String os = System.getProperty("os.name");
                        if (os.contains("Windows")) {
                            command = command.replace("${mvn}", "cmd /C mvn");
                        } else if (os.contains("linux")) {
                            command = command.replace("${mvn}", "sh mvn");
                        } else {
                            throw new RuntimeException("Unsupported operating system");
                        }

                        System.out.println(command);
                        final Process process = Runtime.getRuntime().exec(command);
                        BufferedReader error = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = error.readLine()) != null) {
                            System.out.println(line);
                        }
                        final int ret = process.waitFor();
                        System.out.println("error code:" + ret);
                    }
                }

            }
        } catch (IOException | InterruptedException e) {
            logger.warning(getStackTrace(e));
        }
    }

    private PomInfo parsePomInfo(byte[] pomBytes) {
        final PomInfo pomInfo = new PomInfo();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document document = builder.parse(new ByteArrayInputStream(pomBytes));
            XPath xpath = XPathFactory.newInstance().newXPath();

            Node groupId = parseNode(document, xpath, "/project/groupId", "/project/parent/groupId");

            pomInfo.setGroupId(groupId.getTextContent().trim());

            Node artifactId = parseNode(document, xpath, "/project/artifactId");
            pomInfo.setArtifactId(artifactId.getTextContent().trim());

            Node version = parseNode(document, xpath, "/project/version", "/project/parent/version");
            pomInfo.setVersion(version.getTextContent().trim());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return pomInfo;
    }

    private Node parseNode(Document document, XPath xpath, String... patterns) throws XPathExpressionException {
        Node node;
        for (String p : patterns) {
            node = (Node) xpath.evaluate(p, document, XPathConstants.NODE);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
}
