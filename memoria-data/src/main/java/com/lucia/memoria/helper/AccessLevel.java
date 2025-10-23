package com.lucia.memoria.helper;

/**
 * Access level for a deck.
 */
public enum AccessLevel {
  /**
   * Use parent's access level, or PRIVATE if no parent exists.
   */
  DEFAULT,

  /**
   * Only the owner can see the deck.
   */
  PRIVATE,

  /**
   * Deck is visible to every authenticated user.
   */
  PUBLIC
}
