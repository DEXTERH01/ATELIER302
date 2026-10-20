import axios,{AxiosError,InternalAxiosRequestConfig} from 'axios';
import type {AuthResponse} from '../../types/auth';
import {tokenStore} from '../auth/tokenStore';
const baseURL=import.meta.env.VITE_API_BASE_URL||'/api/v1';
export const api=axios.create({baseURL,withCredentials:true,headers:{'Content-Type':'application/json'}});
const sessionClient=axios.create({baseURL,withCredentials:true});
let refreshPromise:Promise<string>|null=null;
export async function refreshAccessToken(){if(!refreshPromise){refreshPromise=sessionClient.post<AuthResponse>('/auth/refresh').then(({data})=>{tokenStore.set(data.accessToken);return data.accessToken;}).finally(()=>{refreshPromise=null;});}return refreshPromise;}
api.interceptors.request.use((config)=>{const token=tokenStore.get();if(token)config.headers.Authorization=`Bearer ${token}`;return config;});
api.interceptors.response.use(r=>r,async(error:AxiosError)=>{const config=error.config as (InternalAxiosRequestConfig&{_retry?:boolean})|undefined;if(error.response?.status===401&&config&&!config._retry&&!config.url?.startsWith('/auth/')){config._retry=true;try{config.headers.Authorization=`Bearer ${await refreshAccessToken()}`;return api(config);}catch{tokenStore.set(null);}}return Promise.reject(error);});

