package com.edwinclement08.moodlev4;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class DataAPI {

    String boards;
    String faqs;
    String tags;
    OkHttpClient client;

    DataAPI() {
        boards = "[ {name: 'TE-Comps', lastMessage:'essdf', tagColor:'#eee111'}," +
                "{name: 'Robocon', lastMessage:'ghhffghgghfhf', tagColor:'#227222'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}," +
                "{name: 'Vaayushastra', lastMessage:'Elections in Progress', tagColor:'#abcabc'}]";
        faqs = "[ {q: 'Where to we get printing pages', a: 'Printing pages are sold at the office counter', tags:['stationary', 'supplies']}, " +
                " {q: 'Where to we get Assignment sheets', a: 'Assignment sheets are sold at the office counter', tags:['stationary', 'submission']} " +
                "]";

        tags = "{ 'stationary':{'name': 'stationary', 'color':'#782312'}," +
                " 'supplies': {'name': 'supplies', 'color':'#147856'}," +
                " 'submission': {'name': 'submission', 'color':'#252589'}}";
//       client = new OkHttpClient();
//
//       try  {
//           Log.i("JSON", run("https://webhooks.mongodb-stitch.com/api/client/v2.0/app/mcc-jnrsm/service/httpServer/incoming_webhook/webhook0?secret=test"));
//       }    catch (IOException e)   {
//
//       }


    }
    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = this.client.newCall(request).execute();
        return response.body().toString();
    }

    String getBoards() {

        return boards;


    }
    String getTags() {

        return tags;


    }


    String getFaqs()    {
        return faqs;
    }
}
