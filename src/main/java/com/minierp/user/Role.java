package com.minierp.user;

/**
 * Application roles.
 *
 * <ul>
 *   <li>ADMIN / MANAGER: full access (master data, transactions, approvals).</li>
 *   <li>STAFF: transaction input and approvals below the threshold amount.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    MANAGER,
    STAFF
}
