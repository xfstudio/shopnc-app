package com.daxueoo.shopnc.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.daxueoo.shopnc.R;
import com.daxueoo.shopnc.network.adapter.NormalPostRequest;
import com.daxueoo.shopnc.ui.activity.CreateThemeActivity;
import com.daxueoo.shopnc.ui.activity.LoginActivity;
import com.daxueoo.shopnc.utils.ConstUtils;
import com.daxueoo.shopnc.utils.SharedPreferencesUtils;
import com.daxueoo.shopnc.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 放置关于与数据通信的一些方法，与服务器端进行通信等。
 * Created by user on 15-8-2.
 */
public class Shopnc {

    public static final String TAG = "Shopnc";

    /**
     * 判断是否登录并弹出对话框的方法
     *
     * @param context
     */
    public static void isLogin(final Context context) {
        if (SharedPreferencesUtils.getParam(context, "key", "false").equals("false")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);  //先得到构造器
            builder.setTitle("亲，您还未登录呢"); //设置标题
            builder.setMessage("是否立即登录?"); //设置内容
            builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
            builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() { //设置确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.setClass(context, LoginActivity.class);
                    context.startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() { //设置取消按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            //参数都设置完成，创建并显示出来
            builder.create().show();
        }
    }

    public static boolean isHaveKey(Context context) {
        return SharedPreferencesUtils.getParam(context, "key", "false").equals("false");
    }

    /**
     * 登录方法，返回信息
     *
     * @param context
     * @param username
     * @param password
     * @param type
     */
    public static void login(final Context context, String username, String password, String type) {
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", username);
        map.put("password", password);
        map.put("client", type);
        final Request<JSONObject> request = new NormalPostRequest(ConstUtils.LOGIN_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //TODO 处理登录失败
                    JSONObject data = response.getJSONObject("datas");
                    String key = data.getString("key");
                    String username = data.getString("username");
                    String uid = data.getString("uid");

                    SharedPreferencesUtils.setParam(context, "key", key);
                    SharedPreferencesUtils.setParam(context, "username", username);
                    SharedPreferencesUtils.setParam(context, "uid", uid);
                    //  获取用户信息
                    getUserInfo(context, uid);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w(TAG, "response -> " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "登录失败" + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage(), error);
            }
        }, map);
        requestQueue.add(request);
    }

    /**
     * 注册POST表单
     *
     * @param context
     * @param username
     * @param password
     * @param email
     * @param type
     */
    public static void register(final Context context, String username, String password, String email, String type) {
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", username);
        map.put("password", password);
        map.put("password_confirm", password);
        map.put("email", email);
        map.put("client", type);
        final Request<JSONObject> request = new NormalPostRequest(ConstUtils.REGISTER_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("datas");
                    String key = data.getString("key");
                    String username = data.getString("username");

                    SharedPreferencesUtils.setParam(context, "key", key);
                    SharedPreferencesUtils.setParam(context, "username", username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w(TAG, "response -> " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        }, map);
        requestQueue.add(request);
    }

    /**
     * 获取用户信息
     *
     * @param context
     * @param uid
     */
    public static void getUserInfo(final Context context, String uid) {
        Log.e(TAG, ConstUtils.USER_PROFILES_API + "&uid=" + uid);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest objRequest = new JsonObjectRequest(ConstUtils.USER_PROFILES_API + "&uid=" + uid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {

                try {
                    JSONObject jsonObject = obj.getJSONObject("datas");
                    JSONObject userInfo = jsonObject.getJSONObject("user_info");
//                    getUserHeadIcon(context, obj.getString("avatar"));
                    SharedPreferencesUtils.setParam(context, "points", userInfo.getString("member_points"));
                    SharedPreferencesUtils.setParam(context, "email", userInfo.getString("member_email"));
                    SharedPreferencesUtils.setParam(context, "icon_url", userInfo.getString("member_avatar"));
                    SharedPreferencesUtils.setParam(context, "follower_count", userInfo.getString("follower_count"));
                    SharedPreferencesUtils.setParam(context, "following_count", userInfo.getString("following_count"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "获取用户信息失败" + error.getMessage(), Toast.LENGTH_LONG).show();
                error.getMessage();
            }

        });
        requestQueue.add(objRequest);
        requestQueue.start();
    }

}
