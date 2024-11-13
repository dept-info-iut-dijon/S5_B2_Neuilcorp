package com.example.spotthedifference;

/// <summary>
/// Interface contenant toutes les méthodes de Player.
/// </summary>
public interface IPlayer {

    /**
     * Obtient l'identifiant du joueur.
     *
     * @return L'ID du joueur.
     */
    String getPlayerId();

    /**
     * Définit l'identifiant du joueur.
     *
     * @param playerId L'ID du joueur.
     */
    void setPlayerId(String playerId);

    /**
     * Obtient le nom du joueur.
     *
     * @return Le nom du joueur.
     */
    String getName();

    /**
     * Définit le nom du joueur.
     *
     * @param name Le nom du joueur.
     */
    void setName(String name);

    /**
     * Vérifie si le joueur est prêt.
     *
     * @return Vrai si le joueur est prêt, sinon faux.
     */
    boolean isReady();

    /**
     * Définit l'état de préparation du joueur.
     *
     * @param ready Vrai si le joueur est prêt, sinon faux.
     */
    void setReady(boolean ready);
}
