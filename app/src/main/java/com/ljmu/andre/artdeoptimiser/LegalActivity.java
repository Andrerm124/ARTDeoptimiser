package com.ljmu.andre.artdeoptimiser;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LegalActivity extends AppCompatActivity {

	@BindView(R.id.toolbar) Toolbar toolbar;
	Unbinder unbinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_legal);
		unbinder = ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		unbinder.unbind();
	}
}
