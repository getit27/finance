package com.company;

import com.tencent.wework.Finance;
import java.io.File;
import java.io.FileOutputStream;

//10000	�������������������
//10001	������������������
//10002	���ݽ���ʧ��
//10003	ϵͳʧ��
//10004	��Կ�����¼���ʧ��
//10005	fileid����
//10006	����ʧ��
//10007 �Ҳ�����Ϣ���ܰ汾��˽Կ����Ҫ���´���˽Կ��
//10008 ����encrypt_key����
//10009 ip�Ƿ�
//10010 ���ݹ���

public class Main {
    public static void main(String[] args){

        //seq ��ʾ����ҵ�浵��Ϣ��ţ�����ŵ�����������ȡ��Ž�������Ϊ�ϴ���ȡ���ؽ���������š��״���ȡʱseq��0��sdk�᷵����Ч�����������Ϣ��
        //limit ��ʾ������ȡ�������Ϣ������ȡֵ��ΧΪ1~1000
        //proxy��passwdΪ����������������sdk�Ļ�������ֱ�ӷ�����������Ҫ���ô��������sdk���ʵ�������"https://qyapi.weixin.qq.com"��
        //������ͨ��curl����"https://qyapi.weixin.qq.com"����֤����������ȷ���ٴ���sdk��
        //timeout Ϊ��ȡ�Ự�浵�ĳ�ʱʱ�䣬��λΪ�룬���鳬ʱʱ������Ϊ5s��
        //sdkfileid ý���ļ�id���ӽ��ܺ�ĻỰ�浵�еõ�
        //savefile ý���ļ�����·��
        //encrypt_key ��ȡ�Ự�浵���ص�encrypt_random_key��ʹ����������ҵ΢�Ź���̨��rsa��Կ��Ӧ��˽Կ���ܺ�õ�encrypt_key��
        //encrypt_chat_msg ��ȡ�Ự�浵���ص�encrypt_chat_msg
        if (args.length < 2) {
            System.out.println("./sdktools 1(chatmsg) 2(mediadata) 3(decryptdata)\n");
            System.out.println("./sdktools 1 seq limit proxy passwd timeout\n");
            System.out.println("./sdktools 2 fileid proxy passwd timeout savefile\n");
            System.out.println("./sdktools 3 encrypt_key encrypt_chat_msg\n");
            return;
        }

        long ret = 0;
        //ʹ��sdkǰ��Ҫ��ʼ������ʼ���ɹ����sdk����һֱʹ�á�
        //���貢������sdk������ÿ���̳߳���һ��sdkʵ����
        //��ʼ��ʱ�������Լ���ҵ��corpid��secrectkey��
        //ww092ba56a177e8f0f
        //xsw556677
        long sdk = Finance.NewSdk();
        ret = Finance.Init(sdk, "wwd08c8e7c775ab44d", "zJ6k0naVVQ--gt9PUSSEvs03zW_nlDVmjLCTOTAfrew");
        if(ret != 0){
            Finance.DestroySdk(sdk);
            System.out.println("init sdk err ret " + ret);
            return;
        }

        if (args[0].equals("1")) {
            //��ȡ�Ự�浵
            int seq = Integer.parseInt(args[1]);
            int limit = Integer.parseInt(args[2]);
            String proxy = args[3];
            String passwd = args[4];
            int timeout = Integer.parseInt(args[5]);

            //ÿ��ʹ��GetChatData��ȡ�浵ǰ��Ҫ����NewSlice��ȡһ��slice����ʹ����slice�����ݺ󣬻���Ҫ����FreeSlice�ͷš�
            long slice = Finance.NewSlice();
            ret = Finance.GetChatData(sdk, seq, limit, proxy, passwd, timeout, slice);
            if (ret != 0) {
                System.out.println("getchatdata ret " + ret);
                Finance.FreeSlice(slice);
                return;
            }
            System.out.println("getchatdata :" + Finance.GetContentFromSlice(slice));
            Finance.FreeSlice(slice);
        } 
        else if (args[0].equals("2")) {
            //��ȡý���ļ�
            String sdkfileid = args[1];
            String proxy = args[2];
            String passwd = args[3];
            int timeout = Integer.parseInt(args[4]);
            String savefile = args[5];

            //ý���ļ�ÿ����ȡ�����sizeΪ512k����˳���512k���ļ���Ҫ��Ƭ��ȡ�������ļ�δ��ȡ������sdk��IsMediaDataFinish�ӿڻ᷵��0��ͬʱͨ��GetOutIndexBuf�ӿڷ����´���ȡ��Ҫ����GetMediaData��indexbuf��
            //indexbufһ���ʽ���Ҳ���ʾ����Range:bytes=524288-1048575������ʾ�����ȡ���Ǵ�524288��1048575�ķ�Ƭ�������ļ��״���ȡ��д��indexbufΪ���ַ�������ȡ������Ƭʱֱ�������ϴη��ص�indexbuf���ɡ�
            String indexbuf = "";
            while(true){
                //ÿ��ʹ��GetMediaData��ȡ�浵ǰ��Ҫ����NewMediaData��ȡһ��media_data����ʹ����media_data�����ݺ󣬻���Ҫ����FreeMediaData�ͷš�
                long media_data = Finance.NewMediaData();
                ret = Finance.GetMediaData(sdk, indexbuf, sdkfileid, proxy, passwd, timeout, media_data);
                if(ret!=0){
                    System.out.println("getmediadata ret:" + ret);
                    Finance.FreeMediaData(media_data);
                    return;
                }
                System.out.printf("getmediadata outindex len:%d, data_len:%d, is_finis:%d\n",Finance.GetIndexLen(media_data),Finance.GetDataLen(media_data), Finance.IsMediaDataFinish(media_data));
                try {
                    //����512k���ļ����Ƭ��ȡ���˴���Ҫʹ��׷��д���������ķ�Ƭ����֮ǰ�����ݡ�
                    FileOutputStream outputStream  = new FileOutputStream(new File(savefile), true);
                    outputStream.write(Finance.GetData(media_data));
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(Finance.IsMediaDataFinish(media_data) == 1)
                {
                    //�Ѿ���ȡ������һ����Ƭ
                    Finance.FreeMediaData(media_data);
                    break;
                }
                else
                {
                    //��ȡ�´���ȡ��Ҫʹ�õ�indexbuf
                    indexbuf = Finance.GetOutIndexBuf(media_data);
                    Finance.FreeMediaData(media_data);
                }
            }
        } 
        else if (args[0].equals("3")) {
            //���ܻỰ�浵����
            //sdk����Ҫ���û�����rsa˽Կ����֤�û��Ự�浵����ֻ���Լ��ܹ����ܡ�
            //�˴���Ҫ�û�����rsa˽Կ����encrypt_random_key����Ϊencrypt_key��������sdk������encrypt_chat_msg��ȡ�Ự�浵���ġ�
            String encrypt_key = args[1];
            String encrypt_chat_msg = args[2];

            //ÿ��ʹ��DecryptData���ܻỰ�浵ǰ��Ҫ����NewSlice��ȡһ��slice����ʹ����slice�����ݺ󣬻���Ҫ����FreeSlice�ͷš�
            long msg = Finance.NewSlice();
            ret = Finance.DecryptData(sdk, encrypt_key, encrypt_chat_msg, msg);
            if (ret != 0) {
                System.out.println("getchatdata ret " + ret);
                Finance.FreeSlice(msg);
                return;
            }
            System.out.println("decrypt ret:" + ret + " msg:" + Finance.GetContentFromSlice(msg));
            Finance.FreeSlice(msg);
        }
        else {
            System.out.println("wrong args " + args[0]);
        }
        Finance.DestroySdk(sdk);
    }
}

