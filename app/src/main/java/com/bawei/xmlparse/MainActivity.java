package com.bawei.xmlparse;

import android.os.Handler;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bawei.xmlparse.ps_vo.Info;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 网络请求数据后，进行XML解析数据
 */
public class MainActivity extends AppCompatActivity {


    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button mBtnParse = (Button) findViewById(R.id.id_btn_parse);

        mBtnParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //开启次线程进行网络请求
                onHttpRequest("http://apis.juhe.cn/goodbook/catalog?key=9d6ef8c31647a206e05fcaff70527182&dtype=xml");

                //进行解析


            }
        });

    }

    private void onHttpRequest(final String s) {

        //创建OKHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //Request
        Request request = new Request.Builder()
                .url(s)
                .build();
        //new Call
        Call call = okHttpClient.newCall(request);
        //请求调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

                final InputStream inputStream = response.body().byteStream();

//                final List<Info> list = onParsePull(inputStream);
//                final List<Info> list = onParseDom(inputStream);
                final List<Info> list = onParseSax(inputStream);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MainActivity.this, "info"+ list.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }


        });

    }

    /**
     * Sax 解析
     * @param inputStream
     * @return
     */
    private List<Info> onParseSax(InputStream inputStream) {

        List<Info> infoList = new ArrayList<>();

        //创建工厂
        SAXParserFactory factory = SAXParserFactory.newInstance();
        //创建SAXParse
        try {
            SAXParser parser = factory.newSAXParser();
            //获取事件源
            XMLReader xmlReader = parser.getXMLReader();

            MyParseHandler handler = new MyParseHandler(infoList);

            xmlReader.setContentHandler(handler);

            //解析XML文档
            xmlReader.parse(new InputSource(inputStream));

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return infoList;
    }

    /**
     * Dom 解析
     * @param inputStream
     * @return
     */
    private List<Info> onParseDom(InputStream inputStream) {

        List<Info> infoList = new ArrayList<>();

        //创建一个Document解析的工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //创建一个Document Builder
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            //得到解析的Document
            Document document = builder.parse(inputStream);
            //得到Document的element标签
            Element element = document.getDocumentElement();
            //得到节点集合
            NodeList nodeList = element.getElementsByTagName("item");
            //便利
            for (int i = 0; i <nodeList.getLength(); i++) {
                //得到节点
                Element node = (Element) nodeList.item(i);

                NodeList childNodeList = node.getChildNodes();
                Info info= new Info();
                for (int j = 0; j < childNodeList.getLength(); j++) {

                    //得到
                    String nodeName = childNodeList.item(j).getNodeName();


                    if(nodeName .equals("id")){
                        info.setId(Integer.parseInt(childNodeList.item(j).getFirstChild().getNodeValue()));

                    }else if(nodeName .equals("catalog")){
                        info.setCatalog(childNodeList.item(j).getFirstChild().getNodeValue());
                    }


                }

                infoList.add(info);

            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return infoList;

    }



    /**
     * Xml Pull解析
     * @param inputStream
     * @return
     */
    private List<Info> onParsePull(InputStream inputStream) {

        //数据类型
        List<Info> infoList = null;
        Info info = null;

        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(inputStream, "utf-8");

            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch(eventType) {

                    case  XmlPullParser.START_DOCUMENT:

                        //数据集合
                        infoList = new ArrayList<>();
                        System.out.println("Start document");
                    break;


                    case XmlPullParser.START_TAG:

                        switch (xpp.getName()) {

                            case "item": // 判断开始标签元素是否是item

                                info = new Info();
                                break;

                            case "id" :

                                eventType = xpp.next();

                                info.setId(Integer.parseInt(xpp.getText()));// 得到book标签的属性值，并设置book的id
                                break;

                            case "catalog" :

                                eventType = xpp.next();

                                info.setCatalog(xpp.getText());// 得到book标签的属性值，并设置book的id

                                break;

                        }

                        System.out.println("Start tag "+xpp.getName());

                        break;

                    case XmlPullParser.END_TAG:

                        if(xpp.getName() .equals("item")){

                            infoList.add(info);
                            info = null;

                        }

                        System.out.println("End tag "+xpp.getName());

                        break;

                    case XmlPullParser.TEXT:

                        System.out.println("Text "+xpp.getText());

                        break;

                    case XmlPullParser.END_DOCUMENT://文档结束

                        System.out.println("END_DOCUMENT: ");

                        break;
                }


                eventType = xpp.next();

            }

            System.out.println("End document");



        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return infoList;
    }
}
