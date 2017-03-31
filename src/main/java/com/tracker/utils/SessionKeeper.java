package com.tracker.utils;

import java.io.Serializable;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


public class SessionKeeper implements AutoCloseable
{
    private Session session;
    private Transaction transaction;

    private SessionKeeper(Session session, boolean beginTransaction) {
        this.session = session;
        try {
            if ( beginTransaction )
                this.transaction = session.beginTransaction();
        }
        catch ( Throwable err ) {
            this.session.close();
            this.session = null;
            throw err;
        }
    }
    
    public static SessionKeeper open(SessionFactory sf) {
        return new SessionKeeper(sf.openSession(), true);
    }
    
    public static SessionKeeper openWithoutTransaction(SessionFactory sf) {
        return new SessionKeeper(sf.openSession(), false);
    }
    
    public Session getSession() {
        return session;
    }

    public Transaction getTransaction() {
        return transaction;
    }
    
    public void commit() {
        if ( transaction != null )
            transaction.commit();
    }
    
    public void rollback() {
        if ( transaction != null )
            transaction.rollback();
    }
    
    // rollback transaction if it is still active
	public void endTransaction() {
		if ( isTransactionActive() )
		    transaction.rollback();
		transaction = null;
	}
	
	public void beginTransaction() {
		transaction = session.beginTransaction();
	}
	
	public boolean isTransactionActive() {
		return transaction != null && transaction.isActive();
	}
	
    public void commitAndRestart() {
    	if ( isTransactionActive() )
            transaction.commit();
    	restartSessionOnly();
    }
    
    public void commitAndRestartTransaction() {
    	if ( isTransactionActive() )
            transaction.commit();
    	transaction = null;
    	session.clear();
    	transaction = session.beginTransaction();
    }

    public void restart() {
    	endTransaction();
    	restartSessionOnly();
    }
    
    private void restartSessionOnly() {
    	SessionFactory sf = session.getSessionFactory();
        transaction = null;
        session.close();
        session = null;
        // reopen session and start new transaction
        session = sf.openSession();
        transaction = session.beginTransaction();
    }

    // some methods to reduce the need for getSession() calls

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> persistentClass, Serializable id) {
    	if (id == null)
    		return null;
        return (T) session.get(persistentClass, id);
    }

    public Criteria createCriteria(Class<?> persistentClass) {
        return session.createCriteria(persistentClass);
    }
    
    public Criteria createCriteria(Class<?> persistentClass, String alias) {
        return session.createCriteria(persistentClass, alias);
    }
    
    public Query createQuery(String hql) {
        return session.createQuery(hql);
    }
    
    public SQLQuery createSQLQuery(String sql) {
        return session.createSQLQuery(sql);
    }
    
    public void saveOrUpdate(Object object) {
    	session.saveOrUpdate(object);
    }
    
    public void merge(Object object) {
		session.merge(object);	
	}
    
    public void save(Object object) {
    	session.save(object);
    }
    
    public void update(Object object) {
        session.update(object);
    }
    
    public void setFlushMode(FlushMode mode) {
        session.setFlushMode(mode);
    }
    
    @Override
    public void close() throws HibernateException {
        // rollback may throw, so let's be maximally safe 
        try {
            endTransaction();
        }
        finally {
            if ( session != null )
                session.close();
            session = null;
        }
    }
}
