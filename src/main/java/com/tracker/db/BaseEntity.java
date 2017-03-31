package com.tracker.db;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.Hibernate;

/**
 * Base class for all entities (contains id)
 */
@MappedSuperclass
public class BaseEntity {

	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    public Integer getId() {
        return id;
    }
	
    public void setId(Integer id) {
        this.id = id;
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
			return false;
		Integer thisId = getId();
		Integer otherId = ((BaseEntity) obj).getId();
		if (thisId == null) {
			if (otherId != null)
				return false;
		} else if (!thisId.equals(otherId))
			return false;
		return true;
	}
	
}
