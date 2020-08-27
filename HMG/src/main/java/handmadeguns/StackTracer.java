package handmadeguns;

public class StackTracer extends RuntimeException {

	public StackTracer()
	{
		super();
	}

	public StackTracer(String message, Throwable cause)
	{
		super(message,
				cause);
	}

	public StackTracer(String message)
	{
		super(message);
	}

	public StackTracer(Throwable cause)
	{
		super(cause);
	}
}
