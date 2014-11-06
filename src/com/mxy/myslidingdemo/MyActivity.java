package com.mxy.myslidingdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity {
    //联系人信息列表
    private ListView lv_contacts_info_list;
    //联系人姓名集合
    private List<String> contacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        lv_contacts_info_list = (ListView) findViewById(R.id.lv_contacts_info_list);
        initData();
    }

    private void initData() {
        //添加联系人姓名
        contacts = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            contacts.add("张" + i);
        }
        //获得屏幕宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        lv_contacts_info_list.setAdapter(new ContactsInfoAdapter(this, displayMetrics.widthPixels));

    }

    private class ContactsInfoAdapter extends BaseAdapter {
        private int temp = -1;
        private int p;

        private Context mContext;
        private int mScreenWidth;

        public ContactsInfoAdapter(Context context, int screenWidth) {
            mContext = context;
            mScreenWidth = screenWidth;
        }

        @Override
        public int getCount() {
            return contacts.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            final ViewHolder viewHolder;
            if (position == contacts.size()) {
                //始终在ListView的最后一个条目放置此布局
                view = View.inflate(mContext, R.layout.contacts_info_add_new, null);
                TextView tv_add_new_location = (TextView) view.findViewById(R.id.tv_add_new_location);
                tv_add_new_location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转到新增地址页面
                        Toast.makeText(mContext, "新增地址", Toast.LENGTH_SHORT).show();
                        contacts.add("张"+position);
                        notifyDataSetChanged();
                    }
                });
                return view;
            } else {
                convertView = View.inflate(mContext, R.layout.contacts_info_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.horizontalScrollView = (HorizontalScrollView) convertView.findViewById(R.id.hsv_list_item);
                viewHolder.ll_content = (LinearLayout) convertView.findViewById(R.id.ll_content);
                viewHolder.ll_action = (LinearLayout) convertView.findViewById(R.id.ll_action);
                viewHolder.tv_delete = (TextView) convertView.findViewById(R.id.tv_delete);
                viewHolder.rb_info_checked = (RadioButton) convertView.findViewById(R.id.rb_info_checked);
                viewHolder.ll_contacts_info = (LinearLayout) convertView.findViewById(R.id.ll_contacts_info);
                viewHolder.tv_contacts_info_name = (TextView) convertView.findViewById(R.id.tv_contacts_info_name);
                viewHolder.tv_contacts_info_sex = (TextView) convertView.findViewById(R.id.tv_contacts_info_sex);
                viewHolder.tv_contacts_info_phone = (TextView) convertView.findViewById(R.id.tv_contacts_info_phone);
                viewHolder.tv_contacts_info_address = (TextView) convertView.findViewById(R.id.tv_contacts_info_address);
                viewHolder.iv_contacts_info_edit = (ImageView) convertView.findViewById(R.id.iv_contacts_info_edit);

                //设置显示的view宽度为屏幕宽度，将隐藏部分挤出
                ViewGroup.LayoutParams lp = viewHolder.ll_content.getLayoutParams();
                viewHolder.rb_info_checked.setId(position);
                viewHolder.horizontalScrollView.setId(position + 100);
                lp.width = mScreenWidth;
                convertView.setTag(viewHolder);

                //设置触摸监听事件
                convertView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                //获得水平滑动方向值
                                int scrollX = viewHolder.horizontalScrollView.getScrollX();
                                //获得隐藏区域的宽度
                                int actionWidth = viewHolder.ll_action.getWidth();
                                if (scrollX < actionWidth / 2) {
                                    //复原
                                    viewHolder.horizontalScrollView.smoothScrollTo(0, 0);
                                } else {
                                    //滑出 当一个item条目滑出时 上一个滑出的复原
                                    if (p == 0) {
                                        //第一个滑出的条目，只进行滑出操作
                                        viewHolder.horizontalScrollView.smoothScrollTo(actionWidth, 0);
                                        p = position + 100;
                                    } else {
                                        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) MyActivity.this.findViewById(p);
                                        //如果之前滑动的条目被删除，则无法找到相应的horizontalScrollView，所以不进行移动
                                        if (horizontalScrollView != null)
                                            horizontalScrollView.smoothScrollTo(0, 0);
                                        horizontalScrollView = (HorizontalScrollView) MyActivity.this.findViewById(position + 100);
                                        horizontalScrollView.smoothScrollTo(actionWidth, 0);
                                        p = position + 100;
                                    }
                                }
                                return true;
                        }
                        return false;
                    }
                });
                //防止删除一条item后，ListView处于操作状态而进行还原
                if (viewHolder.horizontalScrollView.getScrollX() != 0) {
                    viewHolder.horizontalScrollView.smoothScrollTo(0, 0);
                }
                //设置姓名
                viewHolder.tv_contacts_info_name.setText(contacts.get(position));
                //删除监听
                viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contacts.remove(position);
                        //设置选中下方条目，删除上方条目时选中状态的跟随
                        if (position < temp) {
                            // 两个条目相邻
                            if (temp - position == 1) {
                                viewHolder.rb_info_checked.setChecked(true);
                            } else {
                                //两个条目不相邻，因为一次删除一条，找到删除前选中条目的上一个条目，设置选中状态为true
                                viewHolder.rb_info_checked = (RadioButton) MyActivity.this.findViewById(temp - 1);
                                viewHolder.rb_info_checked.setChecked(true);
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
                //点击联系人信息
                viewHolder.ll_contacts_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //点击条目选中，不能二次点击取消
                        viewHolder.rb_info_checked.setChecked(true);
                        //点击条目时刷新，所有滑出回复原样
                        //notifyDataSetChanged();

                        //如果不是首次点击时才进行此操作
                        if (p != 0) {
                            //点击条目时，上一个滑出的复原
                            HorizontalScrollView horizontalScrollView = (HorizontalScrollView) MyActivity.this.findViewById(p);
                            //如果之前滑动的条目被删除，则无法找到相应的horizontalScrollView，所以不进行移动
                            if (horizontalScrollView != null)
                                horizontalScrollView.smoothScrollTo(0, 0);
                        }
                    }
                });
                //编辑联系人信息
                viewHolder.iv_contacts_info_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //编辑联系人信息
                        Toast.makeText(mContext, "编辑联系人信息", Toast.LENGTH_SHORT).show();
                    }
                });

                //控制单选状态，同滑出 复原
                viewHolder.rb_info_checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (temp != -1) {
                                RadioButton tempRaido = (RadioButton) MyActivity.this.findViewById(temp);
                                if (tempRaido != null) {
                                    tempRaido.setChecked(false);
                                }
                            }
                            temp = buttonView.getId();
                        }
                    }
                });

                if (position == temp) {
                    viewHolder.rb_info_checked.setChecked(true);
                } else {
                    viewHolder.rb_info_checked.setChecked(false);
                }
                return convertView;
            }
        }


        private class ViewHolder {
            //整个view
            HorizontalScrollView horizontalScrollView;
            //联系人信息显示部分
            LinearLayout ll_content;
            //列表隐藏部分
            LinearLayout ll_action;
            //删除列表项
            TextView tv_delete;
            //选择button
            RadioButton rb_info_checked;
            //联系人信息部分
            LinearLayout ll_contacts_info;
            //联系人姓名
            TextView tv_contacts_info_name;
            //联系人性别
            TextView tv_contacts_info_sex;
            //联系人电话
            TextView tv_contacts_info_phone;
            //联系人地址
            TextView tv_contacts_info_address;
            //编辑联系人信息
            ImageView iv_contacts_info_edit;

        }
    }
}
