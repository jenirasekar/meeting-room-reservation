<script setup>
import {
  Building, Users, Calendar, Clock, Check, X, ChevronLeft, ChevronRight, ChevronDown,
  Plus, Pencil, Trash2, LogOut, ShieldCheck, BarChart3, Filter, Search, MapPin,
  Mail, Lock, User, Menu, AlertCircle, CheckCircle, Info, Star, Home, Settings,
  ArrowLeft, ArrowRight, RefreshCw, MoreVertical, Eye, Sparkles, Smile, Upload,
} from 'lucide-vue-next'

const props = defineProps({
  icon: { type: String, required: true },
  size: { type: [String, Number], default: 20 },
  class: { type: String, default: '' },
})

const icons = {
  building:           Building,
  users:              Users,
  calendar:           Calendar,
  clock:              Clock,
  check:              Check,
  'x-mark':           X,
  'chevron-left':     ChevronLeft,
  'chevron-right':    ChevronRight,
  'chevron-down':     ChevronDown,
  plus:               Plus,
  pencil:             Pencil,
  trash:              Trash2,
  logout:             LogOut,
  'shield-check':     ShieldCheck,
  'chart-bar':        BarChart3,
  filter:             Filter,
  search:             Search,
  'location-marker':  MapPin,
  mail:               Mail,
  lock:               Lock,
  user:               User,
  menu:               Menu,
  close:              X,
  'exclamation-circle': AlertCircle,
  'check-circle':     CheckCircle,
  'information-circle': Info,
  star:               Star,
  home:               Home,
  settings:           Settings,
  'arrow-left':       ArrowLeft,
  'arrow-right':      ArrowRight,
  refresh:            RefreshCw,
  'dots-vertical':    MoreVertical,
  eye:                Eye,
  sparkles:           Sparkles,
  'emoji-happy':      Smile,
  upload:             Upload,
}

const IconComponent = icons[props.icon] || icons['exclamation-circle']
</script>

<template>
  <component :is="IconComponent" :size="size" :class="[$props.class]" class="shrink-0" />
</template>
