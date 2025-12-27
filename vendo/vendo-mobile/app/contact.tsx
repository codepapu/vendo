import { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TextInput, TouchableOpacity, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function ContactScreen() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    setIsSubmitting(true);
    try {
      // TODO: Implement contact form API call
      await new Promise((resolve) => setTimeout(resolve, 1000));
      Alert.alert('Success', 'Message sent! We\'ll get back to you as soon as possible.');
      setFormData({ name: '', email: '', subject: '', message: '' });
    } catch (error) {
      Alert.alert('Error', 'Failed to send message. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Get in Touch</Text>
        <Text style={styles.subtitle}>
          We'd love to hear from you. Send us a message and we'll respond as soon as possible.
        </Text>
      </View>

      <View style={styles.section}>
        <View style={styles.contactInfo}>
          <Ionicons name="mail" size={20} color="#3b82f6" />
          <Text style={styles.contactText}>support@vendo.com</Text>
        </View>
        <View style={styles.contactInfo}>
          <Ionicons name="call" size={20} color="#3b82f6" />
          <Text style={styles.contactText}>+1 (555) 123-4567</Text>
        </View>
        <View style={styles.contactInfo}>
          <Ionicons name="location" size={20} color="#3b82f6" />
          <Text style={styles.contactText}>
            123 Commerce Street{'\n'}Business City, BC 12345
          </Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Send us a Message</Text>
        <TextInput
          style={styles.input}
          placeholder="Your name"
          value={formData.name}
          onChangeText={(text) => setFormData({ ...formData, name: text })}
        />
        <TextInput
          style={styles.input}
          placeholder="your.email@example.com"
          value={formData.email}
          onChangeText={(text) => setFormData({ ...formData, email: text })}
          keyboardType="email-address"
          autoCapitalize="none"
        />
        <TextInput
          style={styles.input}
          placeholder="Subject"
          value={formData.subject}
          onChangeText={(text) => setFormData({ ...formData, subject: text })}
        />
        <TextInput
          style={[styles.input, styles.textArea]}
          placeholder="Tell us more..."
          value={formData.message}
          onChangeText={(text) => setFormData({ ...formData, message: text })}
          multiline
          numberOfLines={6}
          textAlignVertical="top"
        />
        <TouchableOpacity
          style={[styles.button, isSubmitting && styles.buttonDisabled]}
          onPress={handleSubmit}
          disabled={isSubmitting}
        >
          <Text style={styles.buttonText}>
            {isSubmitting ? 'Sending...' : 'Send Message'}
          </Text>
        </TouchableOpacity>
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
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
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
    marginBottom: 16,
  },
  contactInfo: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  contactText: {
    fontSize: 16,
    color: '#4b5563',
    marginLeft: 12,
    flex: 1,
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    marginBottom: 12,
    backgroundColor: '#fff',
  },
  textArea: {
    height: 120,
    paddingTop: 12,
  },
  button: {
    backgroundColor: '#3b82f6',
    padding: 14,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});

