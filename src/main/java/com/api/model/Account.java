package com.api.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "accounts")
public class Account implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Integer id;

        @Column(name = "owner_name", length = 250)
        @NotNull
        private String ownerName;

        @Column(name = "balance", nullable=false)
        @NotNull
        private Double balance = 0d;

//        @Column(name = "currency", length = 3)
//        @NotNull
//        private String currency;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Double getBalance() {
            return balance;
        }

        public void setBalance(Double balance) {
            this.balance = balance;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((balance == null) ? 0 : balance.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Account other = (Account) obj;
            if (balance == null) {
                if (other.balance != null)
                    return false;
            } else if (!balance.equals(other.balance))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (ownerName == null) {
                if (other.ownerName != null)
                    return false;
            } else if (!ownerName.equals(other.ownerName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Account [id=" + id +
                    ", ownerName=" + ownerName +
                    ", balance=" + balance +
                    "]";
        }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
