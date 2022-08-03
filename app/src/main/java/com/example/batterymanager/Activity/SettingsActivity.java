package com.example.batterymanager.Activity;

import static com.example.batterymanager.Provider.BatteryManagerDBHelper.KEY_SETTING;
import static com.example.batterymanager.Provider.BatteryManagerDBHelper.KEY_SETTING_DATA;
import static com.example.batterymanager.Provider.BatteryManagerDBHelper.TABLE_SETTINGS;
import static com.example.batterymanager.SettingsConstants.getMonthsFromProgress;
import static com.example.batterymanager.SettingsConstants.getPresetFromRadioGroup;
import static com.example.batterymanager.SettingsConstants.getRadioGroupFromTableSetting;
import static com.example.batterymanager.SettingsConstants.getTimeFromTableSetting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.batterymanager.Provider.BatteryManagerDBHelper;
import com.example.batterymanager.R;
import com.example.batterymanager.SettingsConstants;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.NumberFormat;

public class SettingsActivity extends LogActivity {
    SeekBar historyPeriod;
    SwitchMaterial switchChoice;
    EditText finalPercent;
    EditText predictionTime;
    EditText samplingRate;
    SeekBar windowSize;
    CheckBox checkBattery;
    CheckBox checkRateOfChange;
    CheckBox checkTemperature;
    CheckBox checkScreenStatus;

    BatteryManagerDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        initVariables();
        initView();
        setListeners();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    public void initView(){
        historyPeriod=findViewById(R.id.seekBarPeriod);
        historyPeriod.setProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.HISTORY_PERIOD)));
        finalPercent=findViewById(R.id.finalPercent);
        finalPercent.append(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT));
        predictionTime=findViewById(R.id.timePredict);
        predictionTime.append(helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME));
        samplingRate=findViewById(R.id.samplingRate);
        samplingRate.append(helper.getDataFromSettings(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS));
        switchChoice=findViewById(R.id.switch1);
        switchChoice.setChecked(helper.getDataFromSettings(SettingsConstants.TIME_OR_PERCENT).equals("1"));
        windowSize=findViewById(R.id.seekBarWindow);
        windowSize.setProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.WINDOW_SIZE)));
        checkBattery=findViewById(R.id.checkBattery);
        checkBattery.setChecked(getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(0));
        checkRateOfChange=findViewById(R.id.checkRateOfChange);
        checkRateOfChange.setChecked(getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(1));
        checkTemperature=findViewById(R.id.checkTemperature);
        checkTemperature.setChecked(getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(2));
        checkScreenStatus=findViewById(R.id.checkScreenStatus);
        checkScreenStatus.setChecked(getRadioGroupFromTableSetting(helper.getDataFromSettings(SettingsConstants.MAIN_WINDOW_PRESET)).get(3));
        new TextView(SettingsActivity.this).setTextSize(30);
    }

    public void initVariables(){
        helper=new BatteryManagerDBHelper(this);
    }

    public void setListeners() {
        historyPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Change history period to " + getMonthsFromProgress(historyPeriod.getProgress()) + " months ?");
                builder.setCancelable(false);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" + historyPeriod.getProgress() +
                                " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.HISTORY_PERIOD + "\"");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        seekBar.setProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.HISTORY_PERIOD)));
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        switchChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int ch = 0;
                if (isChecked) ch = 1;
                Log.w(TAG, String.valueOf(isChecked));
                helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" + ch +
                        " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.TIME_OR_PERCENT + "\"");
            }
        });

        finalPercent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66) {
                    try {
                        if (Integer.parseInt(String.valueOf(finalPercent.getText())) > 0 && Integer.parseInt(String.valueOf(finalPercent.getText())) < 101) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("Confirm");
                            builder.setMessage("Change final percent to " + finalPercent.getText() + "% ?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" + finalPercent.getText() +
                                            " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.FINAL_PERCENT + "\"");
                                    finalPercent.setFocusable(false);
                                    finalPercent.setFocusable(true);
                                    finalPercent.setFocusableInTouchMode(true);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finalPercent.setText(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT));
                                    finalPercent.setFocusable(false);
                                    finalPercent.setFocusable(true);
                                    finalPercent.setFocusableInTouchMode(true);
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            Log.w(TAG, String.valueOf(finalPercent.getText()));
                            finalPercent.setText(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT));
                            finalPercent.setFocusable(false);
                            finalPercent.setFocusable(true);
                            finalPercent.setFocusableInTouchMode(true);
                        }
                    } catch (Exception ignored) {
                        Log.w(TAG, ignored.toString());
                        finalPercent.setText(helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT));
                        finalPercent.setFocusable(false);
                        finalPercent.setFocusable(true);
                        finalPercent.setFocusableInTouchMode(true);
                    } finally {
                        Log.w(TAG, "Changed final percent to " + helper.getDataFromSettings(SettingsConstants.FINAL_PERCENT));
                    }
                }
                return false;
            }
        });

        predictionTime.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66) {
                    try {
                        if (Integer.parseInt(String.valueOf(predictionTime.getText())) > 0 && Integer.parseInt(String.valueOf(predictionTime.getText())) < 300) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("Confirm");
                            builder.setMessage("Change prediction time to " + predictionTime.getText() + " minutes?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" + predictionTime.getText() +
                                            " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.PREDICTION_TIME + "\"");
                                    predictionTime.setFocusable(false);
                                    predictionTime.setFocusable(true);
                                    predictionTime.setFocusableInTouchMode(true);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    predictionTime.setText(helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME));
                                    predictionTime.setFocusable(false);
                                    predictionTime.setFocusable(true);
                                    predictionTime.setFocusableInTouchMode(true);
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            Log.w(TAG, String.valueOf(predictionTime.getText()));
                            predictionTime.setText(helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME));
                            predictionTime.setFocusable(false);
                            predictionTime.setFocusable(true);
                            predictionTime.setFocusableInTouchMode(true);
                        }
                    } catch (Exception ignored) {
                        Log.w(TAG, ignored.toString());
                        predictionTime.setText(helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME));
                        predictionTime.setFocusable(false);
                        predictionTime.setFocusable(true);
                        predictionTime.setFocusableInTouchMode(true);
                    } finally {
                        Log.w(TAG, "Changed final percent to " + helper.getDataFromSettings(SettingsConstants.PREDICTION_TIME));
                    }
                }
                return false;
            }
        });

        samplingRate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66) {
                    try {
                        if (Integer.parseInt(String.valueOf(samplingRate.getText())) > 0 && Integer.parseInt(String.valueOf(samplingRate.getText())) < 301) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("Confirm");
                            builder.setMessage("Change sampling interval to " + samplingRate.getText() + " seconds?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" + samplingRate.getText() +
                                            " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS + "\"");
                                    samplingRate.setFocusable(false);
                                    samplingRate.setFocusable(true);
                                    samplingRate.setFocusableInTouchMode(true);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    samplingRate.setText(helper.getDataFromSettings(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS));
                                    samplingRate.setFocusable(false);
                                    samplingRate.setFocusable(true);
                                    samplingRate.setFocusableInTouchMode(true);
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            Log.w(TAG, String.valueOf(samplingRate.getText()));
                            samplingRate.setText(helper.getDataFromSettings(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS));
                            samplingRate.setFocusable(false);
                            samplingRate.setFocusable(true);
                            samplingRate.setFocusableInTouchMode(true);
                        }
                    } catch (Exception ignored) {
                        Log.w(TAG, ignored.toString());
                        samplingRate.setText(helper.getDataFromSettings(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS));
                        samplingRate.setFocusable(false);
                        samplingRate.setFocusable(true);
                        samplingRate.setFocusableInTouchMode(true);
                    } finally {
                        Log.w(TAG, "Changed final percent to " + helper.getDataFromSettings(SettingsConstants.SAMPLING_RATE_FOR_SCREEN_STATUS));
                    }
                }
                return false;
            }
        });

        windowSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Change graph window size to " + getTimeFromTableSetting(windowSize.getProgress()) + " ?");
                builder.setCancelable(false);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" + windowSize.getProgress() +
                                " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.WINDOW_SIZE + "\"");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        seekBar.setProgress(Integer.parseInt(helper.getDataFromSettings(SettingsConstants.WINDOW_SIZE)));
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        setCheckButtonOnCheckedChangeListener(checkBattery);
        setCheckButtonOnCheckedChangeListener(checkRateOfChange);
        setCheckButtonOnCheckedChangeListener(checkTemperature);
        setCheckButtonOnCheckedChangeListener(checkScreenStatus);
    }

    void setCheckButtonOnCheckedChangeListener(CheckBox checkBox){
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                helper.queryWriteable("UPDATE " + TABLE_SETTINGS + " SET " + KEY_SETTING_DATA + "=" +
                        getPresetFromRadioGroup(checkBattery.isChecked(),checkRateOfChange.isChecked(),checkTemperature.isChecked(),checkScreenStatus.isChecked()) +
                        " WHERE " + KEY_SETTING + "=\"" + SettingsConstants.MAIN_WINDOW_PRESET+ "\"");
            }
        });
    }

}