package edu.cmu.sv.arm;

public interface AsyncTaskCompleteListener <T>{
	void onTaskCompleted(T result);
}
