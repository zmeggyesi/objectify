package com.googlecode.objectify.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.test.util.TestBase;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * This test was contributed: https://code.google.com/p/objectify-appengine/issues/detail?id=144
 */
public class LoadCyclesComplexTest extends TestBase {

	@Entity
	public static class A {
		@Id
		public long id = 1;

		AEm aem = new AEm();
	}

	@Embed
	public static class AEm {
		@Load
		public List<Ref<B>> bs = new ArrayList<Ref<B>>();
	}

	@Entity
	public static class B {
		@Parent
		@Load
		public Ref<A> a;

		@Id
		public long id = 2;

		@Load
		public List<Ref<C>> cs = new ArrayList<Ref<C>>();
	}

	@Entity
	public static class C {
		@Parent
		@Load
		public Ref<A> a;

		@Id
		public long id = 3;
	}



	@Test
	public void loadCycles() {
		this.fact.register(A.class);
		this.fact.register(B.class);
		this.fact.register(C.class);

		A a = new A();
		B b = new B();
		C c = new C();

		c.a = Ref.create(a);
		b.a = Ref.create(a);
		b.cs.add(Ref.create(c));
		a.aem.bs.add(Ref.create(b));

		ofy().save().entities(a, b, c).now();
		ofy().clear();

		A a1 = ofy().load().entity(a).get();
		assert a1.aem.bs.get(0).get().cs.get(0).get().a != null;
	}
}
