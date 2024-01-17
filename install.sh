#!/bin/bash

# On prends en paramètre le nom de la machine sur lequel on est

# Sauvegarder au prochain démarrage sudo -s iptables-save -c

# adresse ip du routeur :
# inet : 1.2.3.4
# local : 192.168.2.1
# dmz : 10.0.2.1

#nom des machines :
#routeur : sotoca
#serveur : samba
#client1 : danso
#client2 : wahi
#autre : medina

domaine=raytracing.fr

case $1 in
  routeur)
      echo "Configuration du routeur . . ."
      nmcli con mod local autoconnect true ipv4.method manual ipv4.addresses 192.168.2.1/24
      nmcli con mod dmz autoconnect true ipv4.method manual ipv4.addresses 10.0.2.1/24
      nmcli con mod inet autoconnect true ipv4.method manual ipv4.addresses 1.2.3.4/24 ipv4.gateway
      sysctl net.ipv4.conf.all.forwarding=1
      tar xvPpzf fichiers-routeur.tar.gz
      systemctl restart NetworkManager
      newHostname=sotoca
  ;;
  serveur)
      echo "Configuration du serveur . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      newHostname=samba
  ;;
  client1)
      echo "Configuration du client1 . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      newHostname=danso
  ;;
  client2)
      echo "Configuration du client2 . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      newHostname=wahi

  ;;
  autre)
      echo "Configuration de la machine de l'Internet . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      newHostname=medina
  ;;
  *)
	echo "Mauvais nom de machine ! Veuillez réssayer"
	exit 1
    ;;
esac

echo "$newHostname.$domaine" > /etc/hostname
hostname "$newHostname.$domaine"