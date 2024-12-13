package com.example.spotthedifference.WebSocket;

public interface GameEndedListener {

        /**
         * Méthode appelée lorsque le jeu se fini.
         */
        void onGameEnded(int Attempts , int MissedAttempts);

}
