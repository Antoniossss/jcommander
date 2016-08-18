package com.beust.jcommander;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PostParsingValidationTest {

	abstract class MainParams implements PostParsingValidator {
		@Parameter(names = "-c")
		int count = 0;

		@ParametersDelegate
		InnerValidatingDelegate validatingDelegate = new InnerValidatingDelegate() {

			@Override
			public void validate() throws ParameterException {
			}

		};

		@ParametersDelegate
		InnerDelegate delegate = new InnerDelegate();
	}

	class InnerDelegate {
		@Parameter(names = "-name")
		String name = null;
	}

	abstract class InnerValidatingDelegate implements PostParsingValidator {
		@Parameter(names = "-surname")
		String surname = null;
	}

	@Test
	public void validParameters() {
		AtomicInteger counter = new AtomicInteger();
		MainParams params = new MainParams() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
			}
		};
		JCommander jc = new JCommander(params);
		jc.parse("-c", "5");
		Assert.assertEquals(params.count, 5);
		Assert.assertEquals(counter.get(), 1);
	}

	@Test(expectedExceptions = ParameterException.class, expectedExceptionsMessageRegExp = "Param is invalid")
	public void invalidParameters() {

		MainParams params = new MainParams() {
			@Override
			public void validate() throws ParameterException {
				throw new ParameterException("Param is invalid");
			}
		};
		JCommander jc = new JCommander(params);
		jc.parse("-c", "5");
	}

	@Test(expectedExceptions = ParameterException.class, expectedExceptionsMessageRegExp = "Param is invalid")
	public void invalidInnerDelegate() {
		AtomicInteger counter = new AtomicInteger();
		MainParams params = new MainParams() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
			}
		};
		params.validatingDelegate = new InnerValidatingDelegate() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
				throw new ParameterException("Param is invalid");
			}
		};
		JCommander jc = new JCommander(params);
		jc.parse("-c","4");
	}
	
	@Test
	public void bypassValidation() {
		AtomicInteger counter = new AtomicInteger();
		MainParams params = new MainParams() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
				throw new ParameterException("Validation should not be called");
			}
		};
		params.validatingDelegate = new InnerValidatingDelegate() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
				throw new ParameterException("Validation should not be called");
			}
		};
		JCommander jc = new JCommander(params);
		jc.parseWithoutValidation("-c","4");
		
		Assert.assertEquals(counter.get(), 0);
		Assert.assertEquals(params.count,4);
	}

	@Test
	public void validInnerDelegate() {
		AtomicInteger counter = new AtomicInteger();
		MainParams params = new MainParams() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
			}
		};
		params.validatingDelegate = new InnerValidatingDelegate() {
			@Override
			public void validate() throws ParameterException {
				counter.incrementAndGet();
			}
		};
		JCommander jc = new JCommander(params);
		jc.parse("-name", "Sebastian", "-c", "5", "-surname", "Choina");
		Assert.assertEquals(counter.get(), 2);
		Assert.assertEquals(params.count, 5);
		Assert.assertEquals(params.delegate.name, "Sebastian");
		Assert.assertEquals(params.validatingDelegate.surname, "Choina");
	}

}
