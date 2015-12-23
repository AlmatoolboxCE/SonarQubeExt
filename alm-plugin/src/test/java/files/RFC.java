package files;

public class RFC {
	
//	public RFC() {
//		// TODO Auto-generated constructor stub
//	}
//	
	public RFC(String string) {
		classeB = new String(string);
	}
	
//	public RFC(Integer integer) {
//		// TODO Auto-generated constructor stub
//	}
//	
//	public RFC(Double double1) {
//		// TODO Auto-generated constructor stub
//	}
	
	 private String classeB = new String();
	 
	  public void doSomething(){
	    System.out.println ( "doSomething");
	  }
	  
	  public void doSomethingBasedOnClassB(){
	    System.out.println (classeB.toString());
	  }
	  
//	  class classe2{
//		  public void doSomething_2(){
//			    System.out.print ( "doSomething");
//			  }
//			  
//			  public void doSomethingBasedOnClassB_2(){
//			    System.out.print (classeB.toString().toUpperCase());
//			  }
//	  }

}
