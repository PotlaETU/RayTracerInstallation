#!/bin/bash

# On prends en paramètre le nom de la machine sur lequel on est

# Sauvegarder au prochain démarrage sudo -s iptables-save -c

# adresse ip du routeur :
# inet : 1.2.3.4
# local : 192.168.2.1
# dmz : 20.0.5.1

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
      nmcli con mod local autoconnect true ipv4.method manual ipv4.addresses 192.168.2.1/24 ipv4.routes "20.0.5.0/24 20.0.5.1" ipv4.gateway 1.2.3.4
      nmcli con mod dmz autoconnect true ipv4.method manual ipv4.addresses 20.0.5.1/24 ipv4.routes "192.168.2.0/24 192.168.2.1" ipv4.gateway 1.2.3.4
      nmcli con mod inet autoconnect true ipv4.method manual ipv4.addresses 1.2.3.4/24 ipv4.routes "20.0.5.0/24 20.0.5.1 192.168.2.0/24 192.168.2.1"
      iptables -F
      iptables -A INPUT -j REJECT
      iptables -A OUTPUT -j REJECT
      iptables -A INPUT -s 192.168.2.0/24 -j ACCEPT
      iptables -A INPUT -s 20.0.5.1/24 -j ACCEPT
      iptables -A FORWARD -i dmz -o inet -j ACCEPT
      iptables -A FORWARD -i inet -o dmz -j ACCEPT
      iptables -A FORWARD -i local -o dmz -j ACCEPT
      iptables -A FORWARD -i dmz -o local -j ACCEPT
      iptables -A OUTPUT -s 192.168.2.0/24 -p tcp --dport 80 -j ACCEPT
      iptables -A OUTPUT -s 192.168.2.0/24 -p tcp --dport 443 -j ACCEPT
      iptables -A input
      iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE
      iptables-save > /etc/sysconfig/iptables
      grep -q net.ipv4.conf.all.forwarding=1 /etc/sysctl.conf || cat <<EOF >> /etc/sysctl.conf
net.ipv4.conf.all.forwarding=1
EOF
      tar xvPpzf archivesTAR/fichiers-routeur.tar.gz
      systemctl restart NetworkManager
      systemctl enable --now dhcpd
      newHostname=samba
      echo "Routeur configuré"
  ;;
  serveur)
      echo "Configuration du serveur . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      iptables -F
      iptables -A INPUT -p udp --dport 53 -j ACCEPT
      iptables -A INPUT -p tcp --dport 53 -j ACCEPT
      # J'ai choisi d'autoriser la connexion SSH depuis la machine autre vers le serveur (samba)
      iptables -A INPUT -s 1.2.3.5 -p tcp --dport 22 -j ACCEPT
      iptables -A INPUT -s 1.2.3.5 -p tcp --dport 22 -j LOG --log-prefix "conexion SSH : 1.2.3.5"
      iptables-save > /etc/sysconfig/iptables
      tar xvPpzf archivesTAR/fichiers-serveur.tar.gz
      systemctl enable --now named
      systemctl restart NetworkManager
      tar xvPpzf archivesTAR/serveurJobs.tar.gz
      echo "java -jar /usr/local/serveurJobs.jar &" >> ~/.bashrc
      newHostname=sotoca
      echo "Serveur configuré"
  ;;
  client1)
      echo "Configuration du client1 . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      tar xvPpzf archivesTAR/calculClient.tar.gz
      echo "java -jar /usr/local/calculClient.jar &" >> ~/.bashrc
      newHostname=danso
      echo "Client 1 configuré"
  ;;
  client2)
      echo "Configuration du client2 . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      tar xvPpzf archivesTAR/calculClient.tar.gz
      echo "java -jar /usr/local/calculClient.jar &" >> ~/.bashrc
      newHostname=wahi
      echo "Client 2 configuré"
  ;;
  autre)
      echo "Configuration de la machine de l'Internet . . ."
      nmcli con mod eth0 autoconnect true ipv4.method auto
      systemctl restart NetworkManager
      tar xvPpzf archivesTAR/soumissionJob.tar.gz
      newHostname=medina
      echo "Machine quelconque configurée"
  ;;
  *)
	echo "Mauvais nom de machine ! Veuillez réssayer"
	exit 1
    ;;
esac

echo "$newHostname.$domaine" > /etc/hostname
hostname "$newHostname.$domaine"