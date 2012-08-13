package ie.deri.urq.lidq.benchmark.eval;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class MyNumberFormat extends NumberFormat {


	public static java.text.DecimalFormat twoDForm = new java.text.DecimalFormat("#.##");
	public static java.text.DecimalFormat form = new java.text.DecimalFormat("#");
	NumberFormat fomrat = NumberFormat.getPercentInstance();
	private boolean _showSign =true;

	public MyNumberFormat() {
		this(true);
	}

	public MyNumberFormat(boolean b) {
		_showSign = b;
		fomrat.setMaximumFractionDigits(2);
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo,
			FieldPosition pos) {
		if(_showSign){
			if(number>=0)
				toAppendTo.append("+");
			else{
				toAppendTo.append("-");
			}
		}
		if((number-100)<100)
			toAppendTo.append(twoDForm.format(number-100));
		else
			toAppendTo.append(form.format(number-100));
			
		toAppendTo.append("\\%");
		return toAppendTo;
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo,
			FieldPosition pos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		// TODO Auto-generated method stub
		return null;
	}

}
