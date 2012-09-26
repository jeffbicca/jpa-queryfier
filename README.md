JPA Queryfier
=======================================
This is a library (more precisely, a pair of very simple classes) aimed to simplify the creation of JPA's Query objects which have these following characteristics in particular:

* Be a SELECT query written in JPQL or SQL;
* Have multiple parameters (optionals or not) in the SELECT query.

Motivation
---------------------------------------
As JPA have really moved forward into version 2, it's Criteria API seems a way too verbose and complicated to use, mainly for more elaborated queries. In my opinion, it feels easier to understand and it's clearer to write simple plain old JPQL/SQL than using the API.
But write these queries are boring (and looks dirty too). To create a JPA Query object you have 2 "moments":

1. Create the query object, using the SQL string provided;
2. Bind values to the query parameters specified.

But the code starts getting worse (and dirtier) if some of the parameters are optional and have a null value, as you MUST NOT add them to the Query object. In this case, it is needed:

1. Check if the parameter value is not null to append the "String part" of the query that uses this parameter;
2. Check **(again!)** if the parameter value is not null to bind the value to the correspondent parameter.

Have you got why I've wrote **(again!)**? Because to be able to bind the values, you must create the Query object first...

As I was looking for something very simple and which would not add any additional dependencies into the project I am working and that could simplify this task, I've decided to create this "little monster".

Usage
---------------------------------------
Imagine yourself that you have to develop a method at your Repository class that list "all" users and consider that all these parameters are optional:

String sql = "SELECT u FROM users u WHERE u.name = :name AND u.age >= :minAge AND u.age <= :maxAge"

With JPA Queryfier you could easly do in a single liner:

```java
//...
@Inject EntityManager em;

public List<User> findAllUsersWith(String name, int minimumAge, int maximumAge) {
   return new JpaQueryfier(sql, em).with(name).and(minimumAge).and(maximumAge).queryfy().getResultList();
}
```

JpaQueryfier will use by convention the position of the parameter. It encapsulates the key/value into a QueryParameter object to later append it to the query object.
Optionally, you can specify and create manually the QueryParameter object too:

```java
//...
@Inject EntityManager em;

public List<User> findAllUsersWith(String name, int minimumAge, int maximumAge) {
   return new JpaQueryfier(sql, em).with(new QueryParameter("name", name))
                                    .and(new QueryParameter("minAge", minimumAge))
                                    .and(new QueryParameter("maxAge", maximumAge))
               .queryfy().getResultList();
}
```


Now in plain Java, you would normally have to do something like this...

```java
//...
@Inject EntityManager em;

public List<User> findAllUsersWith(String name, int minimumAge, int maximumAge) {
   String sql = "SELECT u FROM users u WHERE 1=1 "; // lazy to check when to add the WHERE clause
   if(name != null)
      sql += " AND u.name = :name ";
   if(minimumAge != null)
      sql += " AND u.age >= :minAge ";
   if(maximumAge != null)
      sql += " AND u.age <= :maxAge ";

   Query query = em.createQuery(sql);

   if(name != null)
      query.setParameter("name", name);
   if(minimumAge != null)
      query.setParameter("minAge", minimumAge);
   if(maximumAge != null)
      query.setParameter("maxAge", maximumAge);

   return query.getResultList();
}
```

TO DO and limitations
---------------------------------------
* For now, a little bit more complex clauses will not get removed (i.e.: the AND in a BETWEEN clause). A few increments into the PARAMETER_WITH_CLAUSE_REGEX and removeNullParametersFromSql method should be done for this to get able to work. I've addeed the test case for this situation. But for now it is annotated with @Ignore;
* Do an automatic Inject of the EntityManager instead of needing to pass it into parameter;
* Instead of using REGEXes to match the SQL String, use a proper grammar/BNF.

Help Improve and Get Involved
---------------------------------------
I don't consider myself an amazing coder and I know that this work is a very early work in progress. So if you have increments, corrections, polishments, criticisms, sugestions, method renaming, refactoring, documentation or whatever kind of contributions, please submit them via a [Pull Request](https://help.github.com/articles/using-pull-requests) or please initiate a discussion via a new Issue (type `c` after switching focus to the [Issues](https://github.com/jeffbicca/jpa-queryfier/issues) tab).
