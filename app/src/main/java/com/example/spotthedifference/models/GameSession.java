package com.example.spotthedifference.models;

import java.util.*;

/**
 * Représente une session de jeu avec une gestion centralisée de ses propriétés
 * et de son comportement.
 */
public class GameSession {
    private final String sessionId; // Défini à l'intérieur, non modifiable
    private final List<Player> players; // Liste immuable pour éviter les modifications extérieures
    private boolean gameCompleted; // Gestion interne
    private boolean gameTimer; // Gestion interne
    private int imagePairId; // Identifiant de la paire d'images
    private final Map<String, int[]> playerSelections; // Stocke les sélections des joueurs (x, y)
    private final Map<String, Boolean> playerReadyStatus; // Statut "prêt" des joueurs
    private final List<Coordonnees> differenceFound; // Liste des différences trouvées

    /**
     * Constructeur par défaut pour créer une session avec un ID généré automatiquement.
     */
    public GameSession(List<Player> player) {
        this.sessionId = UUID.randomUUID().toString(); // Génère un ID unique
        this.players = player;
        this.playerSelections = new HashMap<>();
        this.playerReadyStatus = new HashMap<>();
        this.differenceFound = new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * Retourne l'ID unique de la session, non modifiable.
     *
     * @return L'ID de la session.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Retourne une liste immuable des joueurs participant à la session.
     *
     * @return Liste immuable des joueurs.
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Ajoute un joueur à la session.
     *
     * @param player Le joueur à ajouter.
     */
    public void addPlayer(Player player) {
        if (player != null && !players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * Supprime un joueur de la session.
     *
     * @param player Le joueur à supprimer.
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Vérifie si le jeu est terminé.
     *
     * @return true si le jeu est terminé, sinon false.
     */
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * Marque la session comme terminée.
     */
    public void setGameCompleted(boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    /**
     * Vérifie si le minuteur est actif.
     *
     * @return true si le minuteur est actif, sinon false.
     */
    public boolean isGameTimer() {
        return gameTimer;
    }

    /**
     * Active ou désactive le minuteur.
     *
     * @param gameTimer true pour activer le minuteur, sinon false.
     */
    public void setGameTimer(boolean gameTimer) {
        this.gameTimer = gameTimer;
    }

    /**
     * Retourne l'identifiant de la paire d'images.
     *
     * @return L'ID de la paire d'images.
     */
    public int getImagePairId() {
        return imagePairId;
    }

    /**
     * Définit l'identifiant de la paire d'images.
     *
     * @param imagePairId L'ID de la paire d'images.
     */
    public void setImagePairId(int imagePairId) {
        this.imagePairId = imagePairId;
    }

    /**
     * Retourne les sélections des joueurs.
     *
     * @return Un dictionnaire des sélections des joueurs.
     */
    public Map<String, int[]> getPlayerSelections() {
        return Collections.unmodifiableMap(playerSelections);
    }

    /**
     * Ajoute ou met à jour une sélection pour un joueur.
     *
     * @param playerId L'ID du joueur.
     * @param coordinates Les coordonnées sélectionnées par le joueur.
     */
    public void setPlayerSelection(String playerId, int[] coordinates) {
        playerSelections.put(playerId, coordinates);
    }

    /**
     * Retourne les statuts de "prêt" des joueurs.
     *
     * @return Un dictionnaire des statuts des joueurs.
     */
    public Map<String, Boolean> getPlayerReadyStatus() {
        return Collections.unmodifiableMap(playerReadyStatus);
    }

    /**
     * Définit le statut "prêt" d'un joueur.
     *
     * @param playerId L'ID du joueur.
     * @param isReady Le statut "prêt".
     */
    public void setPlayerReadyStatus(String playerId, boolean isReady) {
        playerReadyStatus.put(playerId, isReady);
    }

    /**
     * Vérifie si tous les joueurs sont prêts.
     *
     * @return true si tous les joueurs sont prêts, sinon false.
     */
    public boolean areAllPlayersReady() {
        return playerReadyStatus.size() == players.size() && !playerReadyStatus.containsValue(false);
    }

    /**
     * Vérifie si tous les joueurs ont sélectionné une différence.
     *
     * @return true si tous les joueurs ont fait une sélection, sinon false.
     */
    public boolean haveAllPlayersSelected() {
        return playerSelections.size() == players.size();
    }

    /**
     * Retourne les différences trouvées.
     *
     * @return Une liste des différences trouvées.
     */
    public List<Coordonnees> getDifferenceFound() {
        return Collections.unmodifiableList(differenceFound);
    }

    /**
     * Ajoute une différence trouvée.
     *
     * @param coordonnees Les coordonnées de la différence trouvée.
     */
    public void addDifferenceFound(Coordonnees coordonnees) {
        if (coordonnees != null) {
            differenceFound.add(coordonnees);
        }
    }

    /**
     * Vérifie si un joueur est l'hôte de la session.
     *
     * @param playerId L'ID du joueur.
     * @return true si le joueur est l'hôte, sinon false.
     */
    public boolean isHost(String playerId) {
        return !players.isEmpty() && players.get(0).getPlayerId().equals(playerId);
    }

    /**
     * Vérifie si un joueur existe dans la session.
     *
     * @param playerId L'ID du joueur.
     * @return true si le joueur est dans la session, sinon false.
     */
    public boolean containsPlayer(String playerId) {
        return players.stream().anyMatch(player -> player.getPlayerId().equals(playerId));
    }

    @Override
    public String toString() {
        return "GameSession{" +
                "sessionId='" + sessionId + '\'' +
                ", players=" + players +
                ", gameCompleted=" + gameCompleted +
                ", gameTimer=" + gameTimer +
                ", imagePairId=" + imagePairId +
                ", playerSelections=" + playerSelections +
                ", playerReadyStatus=" + playerReadyStatus +
                ", differenceFound=" + differenceFound +
                '}';
    }
}
