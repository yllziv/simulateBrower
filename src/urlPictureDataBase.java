
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.commons.io.IOUtils;

/**
 * ����ҳ��ͼƬ��URLת��Ϊ������ͼƬ�����浽postgres���ݿ��У���������ͼƬ�����ݿ��ж�����������Ϊ����ͼƬ
 */
public class urlPictureDataBase {

    public static void main(String[] args) throws SQLException {
//        insert_mainData();
        getImage();
    }
    /**
     * ����postgres���ݿ�
     */
    public static Connection getConn() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/crawler";
            try {
                conn = DriverManager.getConnection(url, "postgres", "admin");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return conn;
    }

    /**
     * �����ݿ��в�������
     *
     * @param
     */
    public static boolean insert_mainData() throws SQLException {

        System.out.println("*****��ʼ�������ݿ�....\n");

        Connection conn = getConn();
        //conn.setAutoCommit(false);

        System.out.println("*****�������ݿ�ɹ�....\n");

        conn.setAutoCommit(false);

        try {
            byte[] buffer = GetImgFromHttp("http://img4.douban.com/view/event_poster/median/public/ce0616f8eb74107.jpg");

//            String pro_query = "INSERT INTO test VALUES ('"+buffer+"')";
            String pro_query = "INSERT INTO test VALUES (?)";
            PreparedStatement statemenet = conn.prepareStatement(pro_query);
            statemenet.setBytes(1,buffer);
            statemenet.addBatch();

            statemenet.executeBatch();
            conn.commit();//�ύ����
            System.out.println("....�������ݳɹ�!");
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            //ȡ������
            try {
                conn.rollback();
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
        } finally {
            conn.close();
        }

        return true;

    }

    /**
     * ��ͼƬ����Ϊ�������ļ�
     * @param http
     * @return
     */
    public static byte[] GetImgFromHttp(String http) {
        byte[] buffer = null;
        try {
            URL uploadUlr = new URL(http);
            HttpURLConnection connection = (HttpURLConnection)uploadUlr.openConnection();
            connection.setConnectTimeout(10*1000);
            connection.setRequestMethod("GET");
            connection.connect();
            //得到返回的输入流
            InputStream is =  connection.getInputStream();
            int resCode = connection.getResponseCode();
            if (resCode==200) {


                buffer = IOUtils.toByteArray(is);

            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * �����ݿ��ж�ȡ�������ļ�������Ϊ����ͼƬ
     * @param http
     * @return
     */
    public static void getImage() throws SQLException {
        System.out.println("*****��ʼ�������ݿ�....\n");

        Connection conn = getConn();
        //conn.setAutoCommit(false);

        System.out.println("*****�������ݿ�ɹ�....\n");

        Statement statemenet = conn.createStatement();
        conn.setAutoCommit(false);

        try {

            String imgBuffer = "select img from test";
            PreparedStatement prest = conn.prepareStatement(imgBuffer);

            ResultSet result = null;
            prest.execute();
            result = prest.getResultSet();
            result.next();
            result.next();
            byte[] buffer = result.getBytes("img");

            try {
                File file = new File("test.jpg");
                file.createNewFile();
                FileOutputStream os = new FileOutputStream(file);
                os.write(buffer);
                os.flush();
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
        } finally {
            statemenet.close();
            conn.close();
        }
    }
}
