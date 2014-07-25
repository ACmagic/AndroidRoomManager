package edu.cmu.sv.arm;

// Extracting out AsyncTasks into separate classes makes code cleaner
// http://www.jameselsey.co.uk/blogs/techblog/extracting-out-your-asynctasks-into-separate-classes-makes-your-code-cleaner/	
public interface AsyncTaskCompleteListener <T>{
	void onTaskCompleted(T result);
}
