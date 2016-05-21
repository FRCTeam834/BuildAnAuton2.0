
public class enumTest {
	private enum SelectedTool {                                                                                                     
		NONE,                                                                                                                       
		ADD,                                                                                                                        
		EDIT,                                                                                                                       
		DEL                                                                                                                         
	}                                                                                                                               
	SelectedTool tool = SelectedTool.NONE;                                                                                          
	
	public enumTest() {
		System.out.println(tool);
	}
	
	public static void main(String[] args) {
		enumTest t = new enumTest();
	}
	
	
}
