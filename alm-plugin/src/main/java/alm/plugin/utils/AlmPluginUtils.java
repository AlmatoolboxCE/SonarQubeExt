package alm.plugin.utils;

public class AlmPluginUtils {
	
	public static int arrotonda(Double daArrotondare){
		Double parteFrazionaria = daArrotondare % 1;
		if(parteFrazionaria <= 0.5)
			return daArrotondare.intValue();
		else
			return daArrotondare.intValue() + 1;
	}

}
