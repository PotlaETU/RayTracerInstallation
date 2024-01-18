#!/bin/bash

# On prends en paramètre le nom de la machine sur lequel on est

# Sauvegarder au prochain démarrage sudo -s iptables-save -c

# adresse ip du routeur :
# inet : 1.2.3.4
# local : 192.168.2.1
# dmz : 10.0.2.1

#nom des machines :
#routeur : samba
#serveur : sotoca
#client1 : danso
#client2 : wahi
#autre : medina

domaine=raytracing.fr

case $1 in
  routeur)
      echo "Configuration du routeur . . ."
      nmcli con mod local autoconnect true ipv4.method manual ipv4.addresses 192.168.2.1/24 ipv4.routes "192.168.2.0/24 192.168.2.1"
      nmcli con mod dmz autoconnect true ipv4.method manual ipv4.addresses 10.0.2.1/24 ipv4.routes "10.0.2.0/24 10.0.2.1"
      nmcli con mod inet autoconnect true ipv4.method manual ipv4.addresses 1.2.3.4/24
      iptables -F
      iptables -X
      iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE
      grep -q net.ipv4.conf.all.forwarding=1 /etc/sysctl.conf || cat <<EOF >> /etc/sysctl.conf
net.ipv4.conf.all.forwarding=1
EOF
      tar xvPpzf fichiers-routeur.tar.gz
      systemctl restart NetworkManager
      systemctl enable --now dhcpd
      newHostname=samba
  ;;
  serveur)
      echo "Configuration du serveur . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      tar xvPpzf fichiers-serveur.tar.gz
      systemctl enable --now named
      systemctl restart NetworkManager
      newHostname=sotoca
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