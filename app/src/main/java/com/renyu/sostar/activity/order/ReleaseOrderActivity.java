package com.renyu.sostar.activity.order;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.SizeUtils;
import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.commonlibrary.network.OKHttpHelper;
import com.renyu.commonlibrary.network.Retrofit2Utils;
import com.renyu.commonlibrary.network.params.EmptyResponse;
import com.renyu.commonlibrary.views.ActionSheetFragment;
import com.renyu.commonlibrary.views.wheelview.LoopView;
import com.renyu.sostar.R;
import com.renyu.sostar.activity.other.UpdateAddressInfoActivity;
import com.renyu.sostar.activity.other.UpdatePayInfoActivity;
import com.renyu.sostar.activity.other.UpdateTextInfoActivity;
import com.renyu.sostar.activity.other.UpdateTextInfoWithPicActivity;
import com.renyu.sostar.activity.other.UpdateTextinfoWithLabelActivity;
import com.renyu.sostar.activity.other.UpdateTimeInfoActivity;
import com.renyu.sostar.bean.EmployerCashAvaliableRequest;
import com.renyu.sostar.bean.EmployerCashAvaliableResponse;
import com.renyu.sostar.bean.OrderResponse;
import com.renyu.sostar.bean.ReleaseOrderRequest;
import com.renyu.sostar.bean.UploadResponse;
import com.renyu.sostar.impl.RetrofitImpl;
import com.renyu.sostar.params.CommonParams;
import com.renyu.sostar.service.LocationService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

/**
 * Created by renyu on 2017/3/7.
 */

public class ReleaseOrderActivity extends BaseActivity {

    @BindView(R.id.nav_layout)
    RelativeLayout nav_layout;
    @BindView(R.id.tv_nav_title)
    TextView tv_nav_title;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.tv_releaseorder_type)
    TextView tv_releaseorder_type;
    @BindView(R.id.tv_releaseorder_person)
    TextView tv_releaseorder_person;
    @BindView(R.id.tv_releaseorder_sex)
    TextView tv_releaseorder_sex;
    @BindView(R.id.tv_releaseorder_address)
    TextView tv_releaseorder_address;
    @BindView(R.id.tv_releaseorder_desp)
    TextView tv_releaseorder_desp;
    @BindView(R.id.tv_releaseorder_price)
    TextView tv_releaseorder_price;
    @BindView(R.id.tv_releaseorder_paytype)
    TextView tv_releaseorder_paytype;
    @BindView(R.id.tv_releaseorder_time)
    TextView tv_releaseorder_time;
    @BindView(R.id.tv_releaseorder_worktime)
    TextView tv_releaseorder_worktime;
    @BindView(R.id.tv_releaseorder_needmoney)
    TextView tv_releaseorder_needmoney;
    @BindView(R.id.tv_releaseorder_avaliablemoney)
    TextView tv_releaseorder_avaliablemoney;
    @BindView(R.id.sb_releaseorder)
    SwitchButton sb_releaseorder;
    @BindView(R.id.tv_releaseorder_aggregateaddress)
    TextView tv_releaseorder_aggregateaddress;
    @BindView(R.id.tv_releaseorder_aggregatetime)
    TextView tv_releaseorder_aggregatetime;

    // 旧数据
    OrderResponse orderResponse;
    ArrayList<String> picPath;
    ArrayList<ReleaseOrderRequest.ParamBean.PeriodTimeListBean> timeBeans;

    Disposable disposable;

    @Override
    public void initParams() {
        nav_layout.setBackgroundColor(Color.WHITE);
        tv_nav_title.setText("发布订单");
        tv_nav_title.setTextColor(Color.parseColor("#333333"));
        tv_nav_right.setText("保存");
        tv_nav_right.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        picPath=new ArrayList<>();
        timeBeans=new ArrayList<>();
        if (getIntent().getSerializableExtra("value")!=null) {
            orderResponse= (OrderResponse) getIntent().getSerializableExtra("value");

            // 设置用工类型
            tv_releaseorder_type.setText(orderResponse.getJobType());
            tv_releaseorder_type.setVisibility(View.VISIBLE);

            // 设置用工时间
            SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
            String[] periodTimes=orderResponse.getPeriodTime().split(",");
            long lastStart=0;
            ReleaseOrderRequest.ParamBean.PeriodTimeListBean lastBean=null;
            for (int i = 0; i < periodTimes.length; i++) {
                // 开始设置
                if (lastStart==0) {
                    lastBean=new ReleaseOrderRequest.ParamBean.PeriodTimeListBean();
                    lastBean.setStartTime(periodTimes[i]);
                }
                else {
                    try {
                        // 判断是不是连贯的，不是连贯的就将之前的添加进去
                        if ((int) (format.parse(periodTimes[i]).getTime()/1000)-(int) (lastStart/1000)!=3600*24) {
                            lastBean.setEndTime(periodTimes[i-1]);
                            timeBeans.add(lastBean);
                            lastBean=new ReleaseOrderRequest.ParamBean.PeriodTimeListBean();
                            lastBean.setStartTime(periodTimes[i]);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    lastStart=format.parse(periodTimes[i]).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // 设置最后一个
                if (i==periodTimes.length-1) {
                    lastBean.setEndTime(periodTimes[i]);
                    timeBeans.add(lastBean);
                }
            }
            if (timeBeans.size()==0) {
                tv_releaseorder_time.setText("");
            }
            else if (timeBeans.size()==1) {
                tv_releaseorder_time.setText(timeBeans.get(0).getStartTime()+"-"+timeBeans.get(0).getEndTime());
            }
            else {
                tv_releaseorder_time.setText(timeBeans.get(0).getStartTime()+"-"+timeBeans.get(0).getEndTime()+"...");
            }

            // 设置工作时间
            tv_releaseorder_worktime.setText(orderResponse.getStartTime()+"-"+orderResponse.getEndTime());

            // 设置需求人数
            tv_releaseorder_person.setText(""+orderResponse.getStaffAccount());

            // 设置性别要求
            if (orderResponse.getSex().equals("0")) {
                tv_releaseorder_sex.setText("男女不限");
            }
            else if (orderResponse.getSex().equals("1")) {
                tv_releaseorder_sex.setText("男");
            }
            else if (orderResponse.getSex().equals("2")) {
                tv_releaseorder_sex.setText("女");
            }

            // 设置工作地点
            tv_releaseorder_address.setText(orderResponse.getAddress());

            // 设置集合地点
            tv_releaseorder_aggregateaddress.setText(orderResponse.getAggregateAddress());

            // 设置集合时间
            tv_releaseorder_aggregatetime.setText(orderResponse.getAggregateTime());

            // 设置详细描述
            tv_releaseorder_desp.setText(orderResponse.getDescription());
            // 设置详细描述图片
            if (orderResponse.getPicListArray()!=null) {
                picPath.addAll(orderResponse.getPicListArray());
            }

            // 设置工作报酬
            if (orderResponse.getUnitPriceType().equals("2")) {
                tv_releaseorder_price.setText(orderResponse.getUnitPrice()+"/小时");
            }
            else if (orderResponse.getUnitPriceType().equals("1")) {
                tv_releaseorder_price.setText(orderResponse.getUnitPrice()+"/天");
            }

            // 设置结算方式
            tv_releaseorder_paytype.setText(orderResponse.getPaymentType().equals("1")?"日结":"订单结");

            // 设置订单确认
            sb_releaseorder.setChecked(orderResponse.getConfirmFlg().equals("1")?true:false);

            // 设置支付金额
            changeUsedMoney();
        }
        else {
            if (!TextUtils.isEmpty(getIntent().getStringExtra("address"))) {
                // 设置工作地点
                tv_releaseorder_address.setText(getIntent().getStringExtra("address"));
            }
        }
    }

    @Override
    public int initViews() {
        return R.layout.activity_releaseorder;
    }

    @Override
    public void loadData() {
        getEmployerCashAvaliable();
    }

    @Override
    public int setStatusBarColor() {
        return Color.WHITE;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setDark(this);
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.ib_nav_left, R.id.tv_nav_right, R.id.layout_releaseorder_type, R.id.layout_releaseorder_person,
            R.id.layout_releaseorder_sex, R.id.layout_releaseorder_address, R.id.layout_releaseorder_desp,
            R.id.layout_releaseorder_price, R.id.layout_releaseorder_paytype, R.id.layout_releaseorder_time,
            R.id.layout_releaseorder_worktime, R.id.btn_releaseorder_commit, R.id.layout_releaseorder_aggregateaddress,
            R.id.layout_releaseorder_aggregatetime})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_nav_right:
                uploadPic(0);
                break;
            case R.id.ib_nav_left:
                finish();
                break;
            case R.id.layout_releaseorder_type:
                Intent intent_type=new Intent(ReleaseOrderActivity.this, UpdateTextinfoWithLabelActivity.class);
                intent_type.putExtra("title", "用工类型");
                intent_type.putExtra("source", tv_releaseorder_type.getText().toString());
                startActivityForResult(intent_type, CommonParams.RESULT_UPDATELABELINFO);
                break;
            case R.id.layout_releaseorder_person:
                Intent intent_person=new Intent(ReleaseOrderActivity.this, UpdateTextInfoActivity.class);
                intent_person.putExtra("title", "需求人数");
                intent_person.putExtra("param", "staffAccount");
                intent_person.putExtra("needcommit", false);
                intent_person.putExtra("source", tv_releaseorder_person.getText().toString());
                startActivityForResult(intent_person, CommonParams.RESULT_UPDATEUSERINFO);
                break;
            case R.id.layout_releaseorder_sex:
                choiceSex();
                break;
            case R.id.layout_releaseorder_address:
                Intent intent_address=new Intent(ReleaseOrderActivity.this, UpdateAddressInfoActivity.class);
                intent_address.putExtra("title", "工作地点");
                intent_address.putExtra("param", "address");
                intent_address.putExtra("source", tv_releaseorder_address.getText().toString());
                startActivityForResult(intent_address, CommonParams.RESULT_UPDATEADDRESSINFO);
                break;
            case R.id.layout_releaseorder_aggregateaddress:
                Intent intent_aggregateaddress=new Intent(ReleaseOrderActivity.this, UpdateAddressInfoActivity.class);
                intent_aggregateaddress.putExtra("title", "集合地点");
                intent_aggregateaddress.putExtra("param", "aggregateaddress");
                intent_aggregateaddress.putExtra("source", tv_releaseorder_aggregateaddress.getText().toString());
                startActivityForResult(intent_aggregateaddress, CommonParams.RESULT_UPDATEADDRESSINFO);
                break;
            case R.id.layout_releaseorder_desp:
                Intent intent_desp=new Intent(ReleaseOrderActivity.this, UpdateTextInfoWithPicActivity.class);
                intent_desp.putExtra("title", "工作描述");
                intent_desp.putExtra("picPath", picPath);
                intent_desp.putExtra("ed", tv_releaseorder_desp.getText().toString());
                startActivityForResult(intent_desp, CommonParams.RESULT_UPDATEPICINFO);
                break;
            case R.id.layout_releaseorder_price:
                Intent intent_price=new Intent(ReleaseOrderActivity.this, UpdatePayInfoActivity.class);
                intent_price.putExtra("title", "工作报酬");
                intent_price.putExtra("source", tv_releaseorder_price.getText().toString());
                startActivityForResult(intent_price, CommonParams.RESULT_UPDATEPAYTYPEINFO);
                break;
            case R.id.layout_releaseorder_paytype:
                choicePayType();
                break;
            case R.id.layout_releaseorder_time:
                Intent intent_time=new Intent(ReleaseOrderActivity.this, UpdateTimeInfoActivity.class);
                intent_time.putExtra("title", "用工日期");
                intent_time.putExtra("source", timeBeans);
                startActivityForResult(intent_time, CommonParams.RESULT_UPDATETIMEINFO);
                break;
            case R.id.layout_releaseorder_worktime:
                ArrayList<String> hours=new ArrayList<>();
                for (int i=0;i<24;i++) {
                    hours.add(i<10?"0"+i:""+i);
                }
                ArrayList<String> minutes=new ArrayList<>();
                minutes.add("00");
                minutes.add("30");
                View view_timechoice= LayoutInflater.from(ReleaseOrderActivity.this)
                        .inflate(R.layout.view_actionsheet_timechoice, null, false);
                LoopView pop_wheel_timelayout_hour_start= (LoopView) view_timechoice.findViewById(R.id.pop_wheel_timelayout_hour_start);
                LoopView pop_wheel_timelayout_minute_start= (LoopView) view_timechoice.findViewById(R.id.pop_wheel_timelayout_minute_start);
                LoopView pop_wheel_timelayout_hour_end= (LoopView) view_timechoice.findViewById(R.id.pop_wheel_timelayout_hour_end);
                LoopView pop_wheel_timelayout_minute_end= (LoopView) view_timechoice.findViewById(R.id.pop_wheel_timelayout_minute_end);
                ActionSheetFragment.build(getSupportFragmentManager())
                        .setChoice(ActionSheetFragment.CHOICE.CUSTOMER)
                        .setTitle("请选择用工时间")
                        .setOkTitle("确认")
                        .setCancelTitle("取消")
                        .setOnOKListener(value -> {
                            if (Integer.parseInt(hours.get(pop_wheel_timelayout_hour_start.getSelectedItem())+
                                    minutes.get(pop_wheel_timelayout_minute_start.getSelectedItem()))>=
                                    Integer.parseInt(hours.get(pop_wheel_timelayout_hour_end.getSelectedItem())+
                                            minutes.get(pop_wheel_timelayout_minute_end.getSelectedItem()))) {
                                Toast.makeText(this, "用工开始时间不能晚于用工结束时间", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String temp=hours.get(pop_wheel_timelayout_hour_start.getSelectedItem()) + ":" +
                                        minutes.get(pop_wheel_timelayout_minute_start.getSelectedItem()) + "-" +
                                        hours.get(pop_wheel_timelayout_hour_end.getSelectedItem()) + ":" +
                                        minutes.get(pop_wheel_timelayout_minute_end.getSelectedItem());
                                tv_releaseorder_worktime.setText(temp);
                                changeUsedMoney();
                            }
                        })
                        .setOnCancelListener(() -> {

                        })
                        .setCustomerView(view_timechoice)
                        .show();
                pop_wheel_timelayout_hour_start.setNotLoop();
                pop_wheel_timelayout_hour_start.setViewPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(15), SizeUtils.dp2px(20), SizeUtils.dp2px(15));
                pop_wheel_timelayout_hour_start.setItems(hours);
                pop_wheel_timelayout_hour_start.setTextSize(18);
                pop_wheel_timelayout_minute_start.setNotLoop();
                pop_wheel_timelayout_minute_start.setViewPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(15), SizeUtils.dp2px(20), SizeUtils.dp2px(15));
                pop_wheel_timelayout_minute_start.setItems(minutes);
                pop_wheel_timelayout_minute_start.setTextSize(18);
                pop_wheel_timelayout_hour_end.setNotLoop();
                pop_wheel_timelayout_hour_end.setViewPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(15), SizeUtils.dp2px(20), SizeUtils.dp2px(15));
                pop_wheel_timelayout_hour_end.setItems(hours);
                pop_wheel_timelayout_hour_end.setTextSize(18);
                pop_wheel_timelayout_minute_end.setNotLoop();
                pop_wheel_timelayout_minute_end.setViewPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(15), SizeUtils.dp2px(20), SizeUtils.dp2px(15));
                pop_wheel_timelayout_minute_end.setItems(minutes);
                pop_wheel_timelayout_minute_end.setTextSize(18);
                break;
            case R.id.btn_releaseorder_commit:
                uploadPic(1);
                break;
            case R.id.layout_releaseorder_aggregatetime:
                ActionSheetFragment.build(getSupportFragmentManager())
                        .setChoice(ActionSheetFragment.CHOICE.TIME)
                        .setTitle("请选择集合时间")
                        .setOkTitle("确认")
                        .setCancelTitle("取消")
                        .setOnOKListener(value -> tv_releaseorder_aggregatetime.setText(value.toString()))
                        .setOnCancelListener(() -> {

                        })
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CommonParams.RESULT_UPDATELABELINFO && resultCode==RESULT_OK) {
            tv_releaseorder_type.setText(data.getStringExtra("value"));
            tv_releaseorder_type.setVisibility(View.VISIBLE);
        }
        if (requestCode==CommonParams.RESULT_UPDATEUSERINFO && resultCode==RESULT_OK) {
            if (data.getStringExtra("param").equals("staffAccount")) {
                tv_releaseorder_person.setText(data.getStringExtra("value"));
                changeUsedMoney();
            }
        }
        if (requestCode==CommonParams.RESULT_UPDATEPICINFO && resultCode==RESULT_OK) {
            picPath=data.getStringArrayListExtra("picPath");
            tv_releaseorder_desp.setText(data.getStringExtra("ed"));
        }
        if (requestCode==CommonParams.RESULT_UPDATEADDRESSINFO && resultCode==RESULT_OK) {
            if (data.getStringExtra("param").equals("address")) {
                tv_releaseorder_address.setText(data.getStringExtra("value"));
            }
            else {
                tv_releaseorder_aggregateaddress.setText(data.getStringExtra("value"));
            }
        }
        if (requestCode==CommonParams.RESULT_UPDATETIMEINFO && resultCode==RESULT_OK) {
            ArrayList<ReleaseOrderRequest.ParamBean.PeriodTimeListBean> beans= (ArrayList<ReleaseOrderRequest.ParamBean.PeriodTimeListBean>) data.getSerializableExtra("value");
            timeBeans.clear();
            timeBeans.addAll(beans);
            if (timeBeans.size()==0) {
                tv_releaseorder_time.setText("");
            }
            else if (timeBeans.size()==1) {
                tv_releaseorder_time.setText(timeBeans.get(0).getStartTime()+"-"+timeBeans.get(0).getEndTime());
            }
            else {
                tv_releaseorder_time.setText(timeBeans.get(0).getStartTime()+"-"+timeBeans.get(0).getEndTime()+"...");
            }
            changeUsedMoney();
        }
        if (requestCode==CommonParams.RESULT_UPDATEPAYTYPEINFO && resultCode==RESULT_OK) {
            tv_releaseorder_price.setText(data.getStringExtra("value"));
            changeUsedMoney();
        }
    }

    private void choiceSex() {
        View view_clearmessage= LayoutInflater.from(ReleaseOrderActivity.this)
                .inflate(R.layout.view_actionsheet_button_3, null, false);
        ActionSheetFragment actionSheetFragment=ActionSheetFragment.build(getSupportFragmentManager())
                .setChoice(ActionSheetFragment.CHOICE.CUSTOMER)
                .setTitle("设置性别")
                .setCustomerView(view_clearmessage)
                .show();
        TextView pop_three_choice1= (TextView) view_clearmessage.findViewById(R.id.pop_three_choice1);
        pop_three_choice1.setText("男");
        pop_three_choice1.setOnClickListener(v -> {
            tv_releaseorder_sex.setText("男");
            actionSheetFragment.dismiss();
        });
        TextView pop_three_choice2= (TextView) view_clearmessage.findViewById(R.id.pop_three_choice2);
        pop_three_choice2.setText("女");
        pop_three_choice2.setOnClickListener(v -> {
            tv_releaseorder_sex.setText("女");
            actionSheetFragment.dismiss();
        });
        TextView pop_three_cancel= (TextView) view_clearmessage.findViewById(R.id.pop_three_cancel);
        pop_three_cancel.setText("不限");
        pop_three_cancel.setOnClickListener(v -> {
            tv_releaseorder_sex.setText("不限");
            actionSheetFragment.dismiss();
        });
    }

    private void choicePayType() {
        View view_clearmessage= LayoutInflater.from(ReleaseOrderActivity.this)
                .inflate(R.layout.view_actionsheet_button_2, null, false);
        ActionSheetFragment actionSheetFragment=ActionSheetFragment.build(getSupportFragmentManager())
                .setChoice(ActionSheetFragment.CHOICE.CUSTOMER)
                .setTitle("结算方式")
                .setCustomerView(view_clearmessage)
                .show();
        TextView pop_double_choice= (TextView) view_clearmessage.findViewById(R.id.pop_double_choice);
        pop_double_choice.setTextColor(Color.parseColor("#333333"));
        pop_double_choice.setText("日结");
        pop_double_choice.setOnClickListener(v -> {
            tv_releaseorder_paytype.setText("日结");
            actionSheetFragment.dismiss();
        });
        TextView pop_double_cancel= (TextView) view_clearmessage.findViewById(R.id.pop_double_cancel);
        pop_double_cancel.setText("订单结");
        pop_double_cancel.setOnClickListener(v -> {
            tv_releaseorder_paytype.setText("订单结");
            actionSheetFragment.dismiss();
        });
    }

    /**
     * 修改支付总额
     */
    private void changeUsedMoney() {
        // 确保用工人数的存在
        if (TextUtils.isEmpty(tv_releaseorder_person.getText().toString())) {
            tv_releaseorder_needmoney.setText("0");
            return;
        }
        int unitPriceType=-1;
        double unitPrice=-1;
        // 确保单价的存在
        if (TextUtils.isEmpty(tv_releaseorder_price.getText().toString())) {
            tv_releaseorder_needmoney.setText("0");
            return;
        }
        else {
            if (tv_releaseorder_price.getText().toString().split("/")[1].equals("小时")) {
                unitPriceType=2;
            }
            else if (tv_releaseorder_price.getText().toString().split("/")[1].equals("天")) {
                unitPriceType=1;
            }
            unitPrice=Double.parseDouble(tv_releaseorder_price.getText().toString().split("/")[0]);
        }
        // 确保用工日期的存在
        if (timeBeans.size()>0) {
            int allTime=0;
            for (ReleaseOrderRequest.ParamBean.PeriodTimeListBean timeBean : timeBeans) {
                SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
                try {
                    long start=format.parse(timeBean.getStartTime()).getTime();
                    long end=format.parse(timeBean.getEndTime()).getTime();
                    allTime+=(end-start)/(24*3600*1000)+1;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (unitPriceType==1) {
                tv_releaseorder_needmoney.setText(""+allTime*unitPrice*Integer.parseInt(tv_releaseorder_person.getText().toString()));
            }
            else if (unitPriceType==2) {
                // 确保工作时间的存在
                if (!TextUtils.isEmpty(tv_releaseorder_worktime.getText().toString())) {
                    double hourTime=0;
                    SimpleDateFormat format=new SimpleDateFormat("HH:mm");
                    try {
                        long startHour=format.parse(tv_releaseorder_worktime.getText().toString().split("-")[0]).getTime();
                        long endHour=format.parse(tv_releaseorder_worktime.getText().toString().split("-")[1]).getTime();
                        hourTime=((double) (endHour-startHour))/(1000*3600);
                        tv_releaseorder_needmoney.setText(""+allTime*unitPrice*hourTime*Integer.parseInt(tv_releaseorder_person.getText().toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    tv_releaseorder_needmoney.setText("0");
                }
            }
        }
        else {
            tv_releaseorder_needmoney.setText("0");
        }
    }

    private void getEmployerCashAvaliable() {
        EmployerCashAvaliableRequest request=new EmployerCashAvaliableRequest();
        EmployerCashAvaliableRequest.ParamBean paramBean=new EmployerCashAvaliableRequest.ParamBean();
        paramBean.setUserId(Integer.parseInt(ACache.get(this).getAsString(CommonParams.USER_ID)));
        request.setParam(paramBean);
        retrofit.create(RetrofitImpl.class)
                .getEmployerCashAvaiable(Retrofit2Utils.postJsonPrepare(new Gson().toJson(request)))
                .compose(Retrofit2Utils.background()).subscribe(new Observer<EmployerCashAvaliableResponse>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable=d;
            }

            @Override
            public void onNext(EmployerCashAvaliableResponse value) {
                tv_releaseorder_avaliablemoney.setText(""+value.getCashAvaiable());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void uploadPic(int orderStatus) {
        if (TextUtils.isEmpty(tv_releaseorder_type.getText().toString())) {
            Toast.makeText(this, "请选择用工类型", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeBeans.size()==0) {
            Toast.makeText(this, "请添加用工日期", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_worktime.getText().toString())) {
            Toast.makeText(this, "请选择工作时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_person.getText().toString())) {
            Toast.makeText(this, "请填写需求人数", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_sex.getText().toString())) {
            Toast.makeText(this, "请选择性别要求", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_address.getText().toString())) {
            Toast.makeText(this, "请填写工作地点", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_desp.getText().toString())) {
            Toast.makeText(this, "请填写详细描述", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_price.getText().toString())) {
            Toast.makeText(this, "请填写工作报酬", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_type.getText().toString())) {
            Toast.makeText(this, "请选择结算方式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (LocationService.lastBdLocation==null) {
            Toast.makeText(this, "暂无定位数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_aggregateaddress.getText().toString())) {
            Toast.makeText(this, "请填写集合地点", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tv_releaseorder_aggregatetime.getText().toString())) {
            Toast.makeText(this, "请填写集合时间", Toast.LENGTH_SHORT).show();
            return;
        }
        OKHttpHelper helper=new OKHttpHelper();
        String url="http://114.215.18.160:9333/submit";
        Observable.create((ObservableOnSubscribe<ArrayList<String>>) e -> {
            ArrayList<String> images=new ArrayList<>();
            for (String s : picPath) {
                // 网络图片直接添加
                if (s.indexOf("http")!=-1) {
                    images.add(s);
                    continue;
                }
                HashMap<String, File> fileHashMap=new HashMap<>();
                fileHashMap.put("image", new File(s));
                Response resp=helper.syncUpload(fileHashMap, url, new HashMap<>());
                if (resp.isSuccessful()) {
                    Gson gson=new Gson();
                    UploadResponse response=gson.fromJson(resp.body().string(), UploadResponse.class);
                    String imageUrl="http://114.215.18.160:8081/"+response.getFid();
                    images.add(imageUrl);
                }
            }
            e.onNext(images);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ArrayList<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                networkDialg= ProgressDialog.show(ReleaseOrderActivity.this, null, "正在发布");
                disposable=d;
            }

            @Override
            public void onNext(ArrayList<String> value) {
                releaseOrder(value, orderStatus);
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(ReleaseOrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                networkDialg.dismiss();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void releaseOrder(ArrayList<String> images, int orderStatus) {
        ReleaseOrderRequest request=new ReleaseOrderRequest();
        ReleaseOrderRequest.ParamBean paramBean=new ReleaseOrderRequest.ParamBean();
        paramBean.setEndTime(tv_releaseorder_worktime.getText().toString().split("-")[1]);
        paramBean.setStartTime(tv_releaseorder_worktime.getText().toString().split("-")[0]);
        paramBean.setAddress(tv_releaseorder_address.getText().toString());
        paramBean.setConfirmFlg(sb_releaseorder.isChecked()?"1":"0");
        paramBean.setDescription(tv_releaseorder_desp.getText().toString());
        paramBean.setJobType(tv_releaseorder_type.getText().toString());
        paramBean.setLatitude(""+LocationService.lastBdLocation.getLatitude());
        paramBean.setLongitude(""+LocationService.lastBdLocation.getLongitude());
        paramBean.setPaymentType(tv_releaseorder_paytype.getText().toString().equals("日结")?"1":"2");
        paramBean.setPicListArray(images);
        if (tv_releaseorder_sex.getText().toString().equals("男")) {
            paramBean.setSex("1");
        }
        else if (tv_releaseorder_sex.getText().toString().equals("女")) {
            paramBean.setSex("2");
        }
        else {
            paramBean.setSex("0");
        }
        paramBean.setStaffAccount(Integer.parseInt(tv_releaseorder_person.getText().toString()));
        paramBean.setUnitPrice(tv_releaseorder_price.getText().toString().split("/")[0]);
        if (tv_releaseorder_price.getText().toString().split("/")[1].equals("小时")) {
            paramBean.setUnitPriceType("2");
        }
        else if (tv_releaseorder_price.getText().toString().split("/")[1].equals("天")) {
            paramBean.setUnitPriceType("1");
        }
        paramBean.setPeriodTimeList(timeBeans);
        paramBean.setUserId(ACache.get(this).getAsString(CommonParams.USER_ID));
        paramBean.setOrderStatus(""+orderStatus);
        paramBean.setAggregateAddress(tv_releaseorder_aggregateaddress.getText().toString());
        paramBean.setAggregateTime(tv_releaseorder_aggregatetime.getText().toString());
        if (getIntent().getSerializableExtra("value")!=null) {
            paramBean.setOrderId(orderResponse.getOrderId());
        }
        request.setParam(paramBean);
        retrofit.create(RetrofitImpl.class)
                .releaseOrder(Retrofit2Utils.postJsonPrepare(new Gson().toJson(request)))
                .compose(Retrofit2Utils.background()).subscribe(new Observer<EmptyResponse>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable=d;
            }

            @Override
            public void onNext(EmptyResponse value) {
                networkDialg.dismiss();

                // 发单成功刷新首页数据
                // 发单成功刷新详情数据
                EventBus.getDefault().post(new ReleaseOrderRequest());

                Toast.makeText(ReleaseOrderActivity.this, value.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Throwable e) {
                networkDialg.dismiss();
                Toast.makeText(ReleaseOrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
