package mygymapp.gymapp;

import android.widget.RadioButton;

public interface IValidate {
    boolean nameVal(String name);
    boolean bodyTypeVal(RadioButton circuit, RadioButton freeWgt);
    boolean picVal(String fileNam);
}
