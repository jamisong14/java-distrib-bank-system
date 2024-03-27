import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class XMLTest {
    public static void main(String[] args) throws Exception {
        File file = new File("./config.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);

        System.out.println(document.getElementsByTagName("hostname").item(0).getTextContent());
    }
}
