import { View, Text, ScrollView, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function PrivacyScreen() {
  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Ionicons name="shield" size={48} color="#3b82f6" />
        <Text style={styles.title}>Privacy Policy</Text>
        <Text style={styles.date}>
          Last updated: {new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Introduction</Text>
        <Text style={styles.text}>
          At Vendo, we take your privacy seriously. This Privacy Policy explains how we
          collect, use, disclose, and safeguard your information when you visit our
          website and use our services.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Information We Collect</Text>
        <Text style={styles.subsectionTitle}>Personal Information</Text>
        <Text style={styles.text}>
          We may collect personal information that you voluntarily provide to us when
          you register, make a purchase, or use our services, including:
        </Text>
        <Text style={styles.bulletPoint}>• Name and contact information</Text>
        <Text style={styles.bulletPoint}>• Email address</Text>
        <Text style={styles.bulletPoint}>• Billing and shipping addresses</Text>
        <Text style={styles.bulletPoint}>• Payment information</Text>
        <Text style={styles.bulletPoint}>• Phone number</Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>How We Use Your Information</Text>
        <Text style={styles.text}>
          We use the information we collect to process and fulfill your orders, send you
          order confirmations and updates, respond to your inquiries, improve our website
          and services, detect and prevent fraud, and comply with legal obligations.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Data Security</Text>
        <Text style={styles.text}>
          We implement appropriate technical and organizational security measures to
          protect your personal information. However, no method of transmission over
          the Internet is 100% secure.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Your Rights</Text>
        <Text style={styles.text}>
          Depending on your location, you may have the right to access, correct, delete,
          or object to processing of your personal information.
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Contact Us</Text>
        <Text style={styles.text}>
          If you have any questions about this Privacy Policy, please contact us at:
        </Text>
        <Text style={styles.text}>Email: privacy@vendo.com</Text>
        <Text style={styles.text}>Address: 123 Commerce Street, Business City, BC 12345</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    padding: 20,
    backgroundColor: '#f3f4f6',
    alignItems: 'center',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#1f2937',
    marginTop: 12,
    marginBottom: 8,
  },
  date: {
    fontSize: 14,
    color: '#6b7280',
  },
  section: {
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: 12,
  },
  subsectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#374151',
    marginTop: 8,
    marginBottom: 8,
  },
  text: {
    fontSize: 16,
    color: '#4b5563',
    lineHeight: 24,
    marginBottom: 12,
  },
  bulletPoint: {
    fontSize: 16,
    color: '#4b5563',
    lineHeight: 24,
    marginLeft: 16,
    marginBottom: 4,
  },
});

