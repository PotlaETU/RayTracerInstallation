#!/bin/bash

# On prends en paramètre le nom de la machine sur lequel on est

# Sauvegarder au prochain démarrage sudo -s iptables-save -c

# adresse ip du routeur :
# inet : 1.2.3.4
# local : 192.168.2.1
# dmz : 10.0.2.1

case $1 in
  routeur)
      echo "Configuration du routeur . . ."
      nmcli con mod local autoconnect true ipv4.method manual ipv4.addresses 192.168.2.1/24
      nmcli con mod dmz autoconnect true ipv4.method manual ipv4.addresses 10.0.2.1/24
      nmcli con mod inet autoconnect true ipv4.method manual ipv4.addresses 1.2.3.4/24 ipv4.gateway
      net.ipv4.conf.all.forwarding=1
      net.ipv6.conf.all.forwarding=1
      systemctl restart NetworkManager
  ;;
  serveur)
      echo "Configuration du serveur . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
  ;;
  client1)
      echo "Configuration du client1 . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
  ;;
  client2)
      echo "Configuration du client2 . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager

  ;;
  autre)
      echo "Configuration de la machine de l'Internet . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
  ;;
  *)
	echo "Mauvais nom de machine ! Veuillez réssayer"
	exit 1
    ;;
esac