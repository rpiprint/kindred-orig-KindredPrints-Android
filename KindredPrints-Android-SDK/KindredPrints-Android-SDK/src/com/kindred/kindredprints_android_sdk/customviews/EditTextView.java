package com.kindred.kindredprints_android_sdk.customviews;

import com.kindred.kindredprints_android_sdk.R;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class EditTextView extends RelativeLayout {
	public static final int EDIT_TYPE_EMAIL = 0;
	public static final int EDIT_TYPE_PASSWORD = 1;
	
	private Resources resources_;
	private InterfacePrefHelper interfacePrefHelper_;
	private ImageView imgIcon_;
	private EditText editText_;
	
	private TextFieldModifiedListener callback_;
	
	public EditTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.view_edit_text, this, true);
		
		this.resources_ = context.getResources();
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		
		this.imgIcon_ = (ImageView) v.findViewById(R.id.imgIcon);
		this.editText_ = (EditText) v.findViewById(R.id.editTextInput);
		this.editText_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editText_.setBackgroundColor(Color.TRANSPARENT);
	}
	
	public void setTextFieldModifiedListener(TextFieldModifiedListener callback) {
		this.callback_ = callback;
	}
	
	public void setOnEditorActionListener(EditText.OnEditorActionListener listener) {
		this.editText_.setOnEditorActionListener(listener);
	}
	
	public void initEditTextView(int type) {
		if (type == EDIT_TYPE_EMAIL) {
			this.editText_.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			this.editText_.setHint(this.resources_.getString(R.string.login_hint_email));
			this.imgIcon_.setImageDrawable(this.resources_.getDrawable(R.drawable.ico_ampersand_white));
		} else if (type == EDIT_TYPE_PASSWORD) {
			this.editText_.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
			this.editText_.setHint(this.resources_.getString(R.string.login_hint_password));
			this.imgIcon_.setImageDrawable(this.resources_.getDrawable(R.drawable.ico_lock_white));		
		}
		this.editText_.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (callback_ != null) callback_.textFieldWasModified(s.toString());
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) { }
		});
	}
	
	public boolean isBlank() {
		if (this.editText_.getText().length() > 0) {
			return false;
		}
		return true;
	}
	
	public void clearText() {
		this.editText_.setText("");
	}
	
	public String getText() {
		return this.editText_.getText().toString();
	}

	public void setText(String text) {
		this.editText_.setText(text);
	}
	
	public interface TextFieldModifiedListener {
		public void textFieldWasModified(String postChangeText);
	}
}
