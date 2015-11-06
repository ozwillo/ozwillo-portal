package org.oasis_eu.portal.core.model.catalog;

import org.oasis_eu.spring.kernel.model.OrganizationType;

/**
 * User: schambon
 * Date: 5/14/14
 */
public enum Audience {
	CITIZENS {
		@Override
		public boolean isCompatibleWith(OrganizationType orgType) {
			return false;
		}
	},
	PUBLIC_BODIES {
		@Override
		public boolean isCompatibleWith(OrganizationType orgType) {
			return OrganizationType.PUBLIC_BODY.equals(orgType);
		}
	},
	COMPANIES {
		@Override
		public boolean isCompatibleWith(OrganizationType orgType) {
			return OrganizationType.COMPANY.equals(orgType);
		}
	};

	abstract public boolean isCompatibleWith(OrganizationType orgType);
}
